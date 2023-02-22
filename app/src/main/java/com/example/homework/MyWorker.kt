package com.example.homework

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(context: Context, workerParameter: WorkerParameters) : Worker(context,workerParameter) {

    var message = ""

    companion object{
        const val CHANNEL_ID = "channel_id"
        const val NOTIFICATION = 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        Log.d("doWork", "doWork")
        showNotification()
        return Result.success()
    }

    @JvmName("setMessage1")
    private fun setMessage(newMessage: String) {
        message = newMessage
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification() {
        val intent=Intent(applicationContext,MainActivity::class.java).apply {
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext,0,intent,0)

        val notification = Notification.Builder(applicationContext, CHANNEL_ID
        ).setSmallIcon(R.drawable.baseline_message_24)
            .setContentTitle("Reminder")
            .setContentText(message)
            .setPriority(Notification.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "channel name"
            val channelDescription = "channel descriptionm"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID,channelName,channelImportance).apply {
                description = channelDescription
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(NOTIFICATION, notification.build())
        }
    }


}