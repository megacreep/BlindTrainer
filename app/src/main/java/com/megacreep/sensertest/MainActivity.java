package com.megacreep.sensertest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.megacreep.sensertest.media.MusicPlayer;
import com.megacreep.sensertest.sensor.StepDetector;
import com.megacreep.sensertest.sensor.StepDetectorListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccSensor;
    private Sensor mMagSensor;
    private Sensor mStepSensor;
    private Sensor mStepDetectorSensor;
    private Sensor mRotationSensor;
    private Sensor mGyroscopeSensor;

    private SensorEventListener mSensorListener;

    private StepDetector mStepDetector;
    private StepDetectorListener mStepDetectorListener = new StepDetectorListener() {
        @Override
        public void onStepEvent() {
            mPlayer.next();
        }
    };

    private MusicPlayer mPlayer;

    private TextView mTextViewAccValue;
    private TextView mTextViewSpeed;
    private TextView mTextViewDistance;
    private TextView mTextViewMag;
    private TextView mTextViewStep;
    private TextView mTextViewStepDetector;
    private TextView mTextViewRotation;
    private TextView mTextViewAngular;

    private long mStartTime;
    private int mEventListenerSampleRate = SensorManager.SENSOR_DELAY_UI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        try {
            mStepDetector = new StepDetector(mSensorManager);
            mStepDetector.addStepListener(mStepDetectorListener);
        } catch (StepDetector.StepDetectorNotExistException e) {
            Log.d("megacreep", "This device don't support step detector sensor");
        }

        mTextViewAccValue = (TextView) this.findViewById(R.id.tvLinearAccValue);
        mTextViewSpeed = (TextView) this.findViewById(R.id.tvSpeed);
        mTextViewDistance = (TextView) this.findViewById(R.id.tvDistance);
        mTextViewMag = (TextView) this.findViewById(R.id.tvMag);
        mTextViewStep = (TextView) this.findViewById(R.id.tvStep);
        mTextViewStepDetector = (TextView) this.findViewById(R.id.tvStepDetector);
        mTextViewRotation = (TextView) this.findViewById(R.id.tvRotation);
        mTextViewAngular = (TextView) this.findViewById(R.id.tvAngular);

        SeekBar seekBar = (SeekBar) this.findViewById(R.id.seekBar);
        final TextView tvSampleRate = (TextView) this.findViewById(R.id.tvSampleRate);

        seekBar.setMax(100);
        seekBar.setProgress(mEventListenerSampleRate);
        tvSampleRate.setText(Integer.toString(mEventListenerSampleRate));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mEventListenerSampleRate = progress;
                    tvSampleRate.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                rebindListener();
            }
        });

        mPlayer = new MusicPlayer(
                getApplicationContext(),
                new int[]{
                        12, 12, 19, 19, 21, 21, 19,
                        17, 17, 16, 16, 14, 14, 12,
                }
        );
    }

    private void rebindListener() {
        if (mSensorListener != null)
            mSensorManager.unregisterListener(mSensorListener);

        mSensorListener = new MySensorEventListener();
        boolean result;
        result = mSensorManager.registerListener(mSensorListener, mAccSensor, mEventListenerSampleRate);
        if (!result) {
            Log.d("megacreep", "Acc not supported");
        }
        result = mSensorManager.registerListener(mSensorListener, mMagSensor, mEventListenerSampleRate);
        if (!result) {
            Log.d("megacreep", "Mag not supported");
        }
        result = mSensorManager.registerListener(mSensorListener, mRotationSensor, mEventListenerSampleRate);
        if (!result) {
            Log.d("megacreep", "Rotation not supported");
        }
//        result = mSensorManager.registerListener(mSensorListener, mGyroscopeSensor, mEventListenerSampleRate);
//        if (!result) {
//            Log.d("megacreep", "Gyroscope not supported");
//        }

        if (mStepSensor == null) {
            mTextViewStep.setText("Not Supported");
        } else {
            result =mSensorManager.registerListener(mSensorListener, mStepSensor, SensorManager.SENSOR_DELAY_UI);
            if (!result) {
                Log.d("megacreep", "Step not supported");
            }
        }

        if (mStepDetectorSensor == null) {
            mTextViewStepDetector.setText("Not Supported");
        } else {
            result = mSensorManager.registerListener(mSensorListener, mStepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
            if (!result) {
                Log.d("megacreep", "Step Detector not supported");
            }
        }

        mStartTime = System.nanoTime();
    }

    @Override
    protected void onResume() {
        super.onResume();

        rebindListener();
        mStepDetector.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorListener != null) {
            mSensorManager.unregisterListener(mSensorListener);
            mSensorListener = null;
        }

        mStepDetector.stop();
    }

    private class MySensorEventListener implements SensorEventListener {
        private static final float NS2S = 1.0f / 1000000000.0f;
        private static final float EPSILON = 0.1f;
        private static final float ALPHA = 0.8f;
        private static final float PREPARE_TIME = 5;

        private long mLastTimestamp = 0;

        private int mStepCounter = 0;

        private float[] mGravity = new float[3];
        private float[] mAccValues = new float[3];
        private float[] mMagValues = new float[3];
        private float[] mSpeedValues = new float[3];
        private float[] mDistanceValues = new float[3];
        private float[] mCurrentRotation = new float[9];
        private float mTimestamp;

        private final float[] mDeltaRotationVector = new float[4];

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    if (mLastTimestamp != 0) {
                        // time difference between 2 samples
                        float interval = (event.timestamp - mLastTimestamp) * NS2S;

                        // low pass filter
                        mGravity = lowPassFilter(mGravity, event.values);

                        //calculate speed
                        for (int i = 0; i < 3; i++) {
                            mAccValues[i] = event.values[i] - mGravity[i];

                            if ((System.nanoTime() - mStartTime) * NS2S < PREPARE_TIME) {
                                continue;
                            }
                            float newSpeed = calculateNewSpeed(mSpeedValues[i], mAccValues[i], interval);
                            mDistanceValues[i] += calculateNewDistance(mSpeedValues[i], newSpeed, interval);
                            mSpeedValues[i] = newSpeed;
                        }
                    }
                    mLastTimestamp = event.timestamp;
                    mTextViewAccValue.setText(formatData(mAccValues));
                    mTextViewSpeed.setText(formatData(mSpeedValues));
                    mTextViewDistance.setText(formatData(mDistanceValues));
                    return;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagValues = lowPassFilter(mMagValues, event.values);
                    mTextViewMag.setText(formatData(mMagValues));
                    return;
                case Sensor.TYPE_STEP_COUNTER:
                    mTextViewStep.setText(Float.toString(event.values[0]));
                    return;
                case Sensor.TYPE_STEP_DETECTOR:
                    mStepCounter++;
                    mTextViewStepDetector.setText(Integer.toString(mStepCounter));
                    return;
                case Sensor.TYPE_GYROSCOPE:
                    // This timestep's delta rotation to be multiplied by the current rotation
                    // after computing it from the gyro sample data.
                    if (mTimestamp != 0) {
                        final float dT = (event.timestamp - mTimestamp) * NS2S;
                        // Axis of the rotation sample, not normalized yet.
                        float axisX = event.values[0];
                        float axisY = event.values[1];
                        float axisZ = event.values[2];

                        // Calculate the angular speed of the sample
                        double omegaMagnitude = Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                        // Normalize the rotation vector if it's big enough to get the axis
                        // (that is, EPSILON should represent your maximum allowable margin of error)
                        if (omegaMagnitude > EPSILON) {
                            axisX /= omegaMagnitude;
                            axisY /= omegaMagnitude;
                            axisZ /= omegaMagnitude;
                        }

                        // Integrate around this axis with the angular speed by the timestep
                        // in order to get a delta rotation from this sample over the timestep
                        // We will convert this axis-angle representation of the delta rotation
                        // into a quaternion before turning it into the rotation matrix.
                        double thetaOverTwo = omegaMagnitude * dT / 2.0f;
                        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
                        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
                        mDeltaRotationVector[0] = sinThetaOverTwo * axisX;
                        mDeltaRotationVector[1] = sinThetaOverTwo * axisY;
                        mDeltaRotationVector[2] = sinThetaOverTwo * axisZ;
                        mDeltaRotationVector[3] = cosThetaOverTwo;
                    }
                    mTimestamp = event.timestamp;
                    float[] deltaRotationMatrix = new float[9];
                    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, mDeltaRotationVector);
                    // User code should concatenate the delta rotation we computed with the current rotation
                    // in order to get the updated rotation.
                    mCurrentRotation = naiveMatrixMultiply(mCurrentRotation, deltaRotationMatrix);
                    mTextViewAngular.setText(formatRotation(mCurrentRotation));
                    return;
                case Sensor.TYPE_ROTATION_VECTOR:
                    mTextViewRotation.setText(String.format("%.3f, %.3f, %.3f, %.3f", event.values[0], event.values[1], event.values[2], event.values[3]));
                    return;
                default:
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private float calculateNewSpeed(float v, float a, float t) {
            return v + a * t;
        }

        private float calculateNewDistance(float v1, float v2, float t) {
            return (v1 + v2) / 2 * t;
        }
        private String formatData(float[] values) {
            return String.format("%.5f, %.5f, %.5f", values[0], values[1], values[2]);
        }

        private String formatRotation(float[] value) {
            return String.format("%s\n%s\n%s\n",
                    formatData(Arrays.copyOfRange(value, 0, 3)),
                    formatData(Arrays.copyOfRange(value, 3, 6)),
                    formatData(Arrays.copyOfRange(value, 6, 9))
                    );
        }

        private float[] lowPassFilter(float[] oldValues, float[] values) {
            for (int i = 0; i < 3; i++ ) {
                oldValues[i] = ALPHA * oldValues[i] + (1 - ALPHA) * values[i];
            }
            return oldValues;
        }

        /**
         * Performs naiv n^3 matrix multiplication and returns C = A * B
         *
         * @param A Matrix in the array form (e.g. 3x3 => 9 values)
         * @param B Matrix in the array form (e.g. 3x3 => 9 values)
         * @return A * B
         */
        public float[] naiveMatrixMultiply(float[] B, float[] A) {
            int mA, nA, mB, nB;
            mA = nA = (int) Math.sqrt(A.length);
            mB = nB = (int) Math.sqrt(B.length);

            if (nA != mB)
                throw new RuntimeException("Illegal matrix dimensions.");

            float[] C = new float[mA * nB];
            for (int i = 0; i < mA; i++)
                for (int j = 0; j < nB; j++)
                    for (int k = 0; k < nA; k++)
                        C[i + nA * j] += (A[i + nA * k] * B[k + nB * j]);
            return C;
        }
    }
}