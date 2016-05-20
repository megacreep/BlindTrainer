package com.megacreep.sensertest.sensor;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import com.megacreep.sensertest.media.BluetoothController;
import com.megacreep.sensertest.media.MusicPlayer;

/**
 * Created by megacreep on 5/18/2016.
 */
public class TrainingController {

    public interface OnTrainingEventController {
        void onGaitScoreChanged(float score);
        void onDirectionalScoreChanged(float score, boolean isLeft);
        void onBalanceScoreChanged(float score);
        void onStepChanged(int value);
        void onTrainingFinished(boolean success);
    }

    Context mContext;
    OnTrainingEventController mListener;

    private SensorManager mSensorManager;

    private StepDetector mStepDetector;
    private StepDetectorListener mNoteOnStepListener = new StepDetectorListener() {
        private int stepCounter = 0;

        @Override
        public void onStepEvent() {
            if (mPlayer != null) {
                mPlayer.next();
            }
            if (mBluetoothController != null) {
                mBluetoothController.vibrate(1);
            }
            mListener.onStepChanged(++stepCounter);
        }
    };

    private MusicPlayer mPlayer;

    private BluetoothController mBluetoothController;

    private static TrainingController mInstance;

    public static TrainingController getInstance(Context context, OnTrainingEventController listener) {
        if (mInstance == null) {
            mInstance = new TrainingController(context, listener);
        }
        return mInstance;
    }

    private TrainingController(Context context, OnTrainingEventController listener) {
        mContext = context;
        mListener = listener;

        mPlayer = new MusicPlayer(
                context,
                new int[]{
                        12, 12, 19, 19, 21, 21, 19,
                        17, 17, 16, 16, 14, 14, 12,
                }
        );
        mBluetoothController = BluetoothController.getInstance(context);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        try {
            mStepDetector = new StepDetector(mSensorManager);
        } catch (StepDetector.StepDetectorNotExistException e) {
            Log.d("megacreep", "This device don't support step detector sensor");
        }
    }

    public void start(boolean isNoteOnStep) {
        if (mStepDetector != null) {
            if (isNoteOnStep) {
                mStepDetector.addStepListener(mNoteOnStepListener);
            }
            mStepDetector.start();
            mBluetoothController.connect();
        }
    }

    public void abort() {
        mListener.onTrainingFinished(false);
        if (mStepDetector != null) {
            mStepDetector.stop();
        }
    }

    public TrainingResult getResult() {
        return null;
    }

    public class TrainingResult {
        public float score;
        public long duration;
        public char level;
        public float direction;
        public float gait;
        public float balance;
    }
}
