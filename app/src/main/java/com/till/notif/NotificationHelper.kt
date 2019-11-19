package com.till.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.till.MainActivity
import com.till.R
import kotlin.random.Random

object NotificationHelper {
    fun createNotificationChannel(
        context: Context,
        importance: Int,
        showBadge: Boolean,
        name: String,
        description: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "${context.packageName}-testChannel"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun createSampleDataNotification(
        context: Context,
        title: String,
        message: String,
        contactAddress: String = "",
        bigText: String = "",
        autoCancel: Boolean = true
    ): NotificationCompat.Builder {
        val channelId = "${context.packageName}-testChannel"

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)


        val builder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(pendingIntent)
            setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(autoCancel)
        }

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data =
                Uri.parse("smsto:$contactAddress")  // This ensures only SMS apps respond
            putExtra("sms_body", context.getString(R.string.sms_template_omg))
        }

        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$contactAddress")
        }

        // Verify it resolves
        val activities: List<ResolveInfo> =
            context.packageManager.queryIntentActivities(smsIntent, 0)
        val isIntentSafe: Boolean = activities.isNotEmpty()

        val smsPendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                smsIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )

        val callPendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                callIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )

        if (isIntentSafe) {
            builder.addAction(
                R.drawable.ic_launcher_foreground, context.getString(R.string.text),
                smsPendingIntent
            )
        }
        builder.addAction(
            R.drawable.ic_launcher_foreground, context.getString(R.string.call),
            callPendingIntent
        )

        return builder
    }

    fun showNotification(context: Context, builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(Random.nextInt(), builder.build())
        }
    }
}