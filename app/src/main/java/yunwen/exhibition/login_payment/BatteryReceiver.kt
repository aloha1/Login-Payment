package yunwen.exhibition.login_payment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

const val CHANNEL_ID = "CHANNEL_ID"
const val CHANNEL_NAME = "CHANNEL_NAME"
const val NOTIFICATION_ID = 12345

class BatteryReceiver: BroadcastReceiver() {
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
        if(intent?.action.equals("BATTERY_CHANGED")){
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannelId  = CHANNEL_ID
            val channelName = CHANNEL_NAME
            // Create the NotificationChannel, if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

                notificationManager.createNotificationChannel(channel)
            }

            val notificationBuilder
            = NotificationCompat.Builder(context, notificationChannelId)
                .setSmallIcon(R.drawable.ic_alarm)

            .setContentTitle("Notification Title")
                .setContentText("Notification Body")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


            // Create an Intent to open your app
            val intent = Intent(context, LoginActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            notificationBuilder.setContentIntent(pendingIntent)


            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        }
    }

}