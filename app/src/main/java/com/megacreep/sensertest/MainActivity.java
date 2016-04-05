package com.megacreep.sensertest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccSensor;
    private Sensor mMagSensor;

    private SensorEventListener mSensorListener;

    private TextView mTextViewAccValue;
    private TextView mTextViewSpeed;
    private TextView mTextViewDistance;
    private TextView mTextViewMag;

    private SeekBar mSeekBar;

    private long mStartTime;
    private int mEventListenerSampleRate = SensorManager.SENSOR_DELAY_NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mTextViewAccValue = (TextView) this.findViewById(R.id.tvLinearAccValue);
        mTextViewSpeed = (TextView) this.findViewById(R.id.tvSpeed);
        mTextViewDistance = (TextView) this.findViewById(R.id.tvDistance);
        mTextViewMag = (TextView) this.findViewById(R.id.tvMag);
        mSeekBar = (SeekBar) this.findViewById(R.id.seekBar);
        final TextView tvSampleRate = (TextView) this.findViewById(R.id.tvSampleRate);

        mSeekBar.setMax(100);
        mSeekBar.setProgress(mEventListenerSampleRate);
        tvSampleRate.setText(Integer.toString(mEventListenerSampleRate));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    }

    private void rebindListener() {
        if (mSensorListener != null)
            mSensorManager.unregisterListener(mSensorListener);

        mSensorListener = new AccelerometerListener();
        mSensorManager.registerListener(mSensorListener, mAccSensor, mEventListenerSampleRate);
        mSensorManager.registerListener(mSensorListener, mMagSensor, mEventListenerSampleRate);
        mStartTime = System.nanoTime();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorListener = new AccelerometerListener();
        mSensorManager.registerListener(mSensorListener, mAccSensor, mEventListenerSampleRate);
        mSensorManager.registerListener(mSensorListener, mMagSensor, mEventListenerSampleRate);
        mStartTime = System.nanoTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
        mSensorListener = null;
    }

    private class AccelerometerListener implements SensorEventListener {
        private static final float NS2S = 1.0f / 1000000000.0f;
        private static final float EPSILON = 0.1f;
        private static final double ALPHA = 0.8;
        private static final float PREPARE_TIME = 20;

        private long mLastTimestamp = 0;

        private double[] mGravity = new double[3];
        private double[] mAccValues = new double[3];
        private double[] mMagValues = new double[3];
        private double[] mSpeedValues = new double[3];
        private double[] mDistanceValues = new double[3];

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    if (mLastTimestamp != 0) {
                        // time difference between 2 samples
                        float interval = (event.timestamp - mLastTimestamp) * NS2S;

                        // low pass filter
                        mGravity = lowPassFilter(mGravity, event.values);

                        if ((System.nanoTime() - mStartTime) * NS2S < PREPARE_TIME) {
                            return;
                        }
                        //calculate speed
                        for (int i = 0; i < 3; i++) {
                            mAccValues[i] = event.values[i] - mGravity[i];
                            double newSpeed = calculateNewSpeed(mSpeedValues[i], mAccValues[i], interval);
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
                default:
                    break;
            }

            mLastTimestamp = event.timestamp;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private double calculateNewSpeed(double v, double a, float t) {
            return v + a * t;
        }

        private double calculateNewDistance(double v1, double v2, float t) {
            return (v1 + v2) / 2 * t;
        }
        private String formatData(double[] values) {
            return String.format("%.5f, %.5f, %.5f", values[0], values[1], values[2]);
        }

        private double[] lowPassFilter(double[] oldValues, float[] values) {
            for (int i = 0; i < 3; i++ ) {
                oldValues[i] = ALPHA * oldValues[i] + (1 - ALPHA) * values[i];
            }
            return oldValues;
        }
    }
}