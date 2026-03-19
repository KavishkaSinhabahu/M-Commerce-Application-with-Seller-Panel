package com.kavishkasinhabahu.craftshub;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

public class ProximitySensorManager implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private boolean isEnabled = true;

    public ProximitySensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "MyApp::ProximityLock");
        }

        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    public void register() {
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregister() {
        if (proximitySensor != null) {
            sensorManager.unregisterListener(this);
        }
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isEnabled) return;

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < proximitySensor.getMaximumRange()) {
                if (!wakeLock.isHeld()) {
                    wakeLock.acquire();
                }
            } else {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
