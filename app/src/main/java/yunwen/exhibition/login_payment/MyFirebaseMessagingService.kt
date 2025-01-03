package yunwen.exhibition.login_payment
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import yunwen.exhibition.login_payment.LoginActivity.Companion.TAG

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Get message data
        val title = remoteMessage.data["title"] ?: "Notification Title"
        val message = remoteMessage.data["message"] ?: "Notification Message"

        Log.d(TAG, "onMessageReceived: ")

        // Create an Intent for the notification
        val intent = Intent(this, MainActivity::class.java) // Replace with your main activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Create a NotificationChannel (for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "your_channel_id"
            val channelName = "Your Channel Name"
            val channelDescription = "Channel Description"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = channelDescription
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, "your_channel_id")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_alarm) // Replace with your app icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Log and send the FCM token to your server
        // for sending push notifications
        Log.d(TAG, "Refreshed token: $token")

    }
}