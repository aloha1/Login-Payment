package yunwen.exhibition.login_payment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

const val CHANNEL_ID = "CHANNEL_ID"

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val batteryLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val batteryStatus
        = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        // Handle different battery levels and status
        if (batteryLevel != null) {
            Log.d("BatteryLevelReceiver", "Battery Level: $batteryLevel%")
            // Perform actions based on battery level, e.g., show a notification, dim the screen
        }

        if (batteryStatus != null) {
            when (batteryStatus) {
                BatteryManager.BATTERY_STATUS_CHARGING -> Log.d("BatteryLevelReceiver", "Charging")
                BatteryManager.BATTERY_STATUS_DISCHARGING -> Log.d("BatteryLevelReceiver", "Discharging")
                BatteryManager.BATTERY_STATUS_FULL -> Log.d("BatteryLevelReceiver", "Full")
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> Log.d("BatteryLevelReceiver", "Not Charging")
                else -> Log.d("BatteryLevelReceiver", "Unknown Status")
            }
        }
    }

}