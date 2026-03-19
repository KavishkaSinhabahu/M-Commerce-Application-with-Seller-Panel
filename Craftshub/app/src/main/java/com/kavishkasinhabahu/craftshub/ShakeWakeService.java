package com.kavishkasinhabahu.craftshub;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

public class ShakeWakeService extends Service implements ShakeDetector.OnShakeListener {
    private ShakeDetector shakeDetector;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        shakeDetector = new ShakeDetector(this);
        shakeDetector.registerSensor(this);
    }

    @Override
    public void onShake() {
        wakeUpScreen();
    }

    private void wakeUpScreen() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isInteractive()) {
            wakeLock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "ShakeWake:WakeLock"
            );
            wakeLock.acquire(3000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shakeDetector.unregisterSensor(this);
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
