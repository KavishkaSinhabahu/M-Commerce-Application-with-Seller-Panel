package com.kavishkasinhabahu.craftshub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class FlightModeReceiver extends BroadcastReceiver {

    private AlertDialog flightModeDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
            boolean isFlightModeOn = Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0;

            if (isFlightModeOn) {
                Log.d("FlightModeReceiver", "Flight mode activated.");
                Toast.makeText(context, "Flight mode activated", Toast.LENGTH_SHORT).show();

                if (flightModeDialog == null || !flightModeDialog.isShowing()) {
                    flightModeDialog = new AlertDialog.Builder(context)
                            .setTitle("Flight Mode Activated")
                            .setMessage("Your device is in flight mode. Please turn it off to continue.")
                            .setCancelable(false)
                            .show();
                }
            } else {
                Log.d("FlightModeReceiver", "Flight mode deactivated.");
                Toast.makeText(context, "Flight mode deactivated", Toast.LENGTH_SHORT).show();

                if (flightModeDialog != null && flightModeDialog.isShowing()) {
                    flightModeDialog.dismiss();
                    flightModeDialog = null;
                }
            }
        }
    }

}