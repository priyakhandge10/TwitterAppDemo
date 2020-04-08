package com.example.twitterappdemo

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FirebaseMessagingService"

        @SuppressLint("LongLogTag")
        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            Log.d(TAG, "From: ${remoteMessage.from}")

            if (remoteMessage.notification!= null) {
                sendNotification(remoteMessage.notification.toString())
            }

        }
        private fun sendNotification(messageBody: String) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this )
                .setSmallIcon(R.drawable.twitterlogo)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }
    }

