package com.megacreep.sensertest.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StepDetector {

    private SensorManager mManager;
    private Set<StepDetectorListener> mListeners = new HashSet<>();
    private Sensor mSensor;
    private SensorEventListener mEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == mSensor.getType()) {
                for (StepDetectorListener listener: mListeners) {
                    listener.onStepEvent();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public StepDetector(SensorManager manager) throws StepDetectorNotExistException {
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (sensor == null) {
            Log.d("megacreep", "Step Detector not support");
            sensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        if (sensor == null) {
            throw new StepDetectorNotExistException();
        }

        mManager = manager;
        mSensor = sensor;
    }

    public void start() {
        mManager.registerListener(mEventListener, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        mManager.unregisterListener(mEventListener);
        clearListeners();
    }

    public void addStepListener(StepDetectorListener listener) {
        mListeners.add(listener);
    }

    public void removeStepListener(StepDetectorListener listener) {
        mListeners.remove(listener);
    }

    public void clearListeners() {
        mListeners.clear();
    }

    public class StepDetectorNotExistException extends Exception {
    }
}

