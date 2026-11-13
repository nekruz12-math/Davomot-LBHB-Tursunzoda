package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

class MorningReminderReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
      scheduleDailyMorningReminder(context)
      return
    }

    // Only fire on school days (Monday..Saturday)
    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    if (dayOfWeek != Calendar.SUNDAY) {
      showMorningNotification(context)
    }
    // Reschedule for next morning
    scheduleDailyMorningReminder(context)
  }

  companion object {
    private const val CHANNEL_ID = "morning_attendance_channel"
    private const val NOTIFICATION_ID = 1001
    private const val ALARM_REQUEST_CODE = 2001

    fun scheduleDailyMorningReminder(context: Context, hour: Int = 8, minute: Int = 5) {
      try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MorningReminderReceiver::class.java).apply {
          action = "com.example.ACTION_MORNING_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          ALARM_REQUEST_CODE,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val targetCal = Calendar.getInstance().apply {
          set(Calendar.HOUR_OF_DAY, hour)
          set(Calendar.MINUTE, minute)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
          if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
          }
        }

        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          targetCal.timeInMillis,
          pendingIntent
        )
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    fun showMorningNotification(context: Context) {
      try {
        val notificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val channel = NotificationChannel(
            CHANNEL_ID,
            "Ёдоварии давомоти субҳона",
            NotificationManager.IMPORTANCE_DEFAULT
          ).apply {
            description = "Ҳар саҳар ёдоварӣ барои қайди давомоти хонандагон"
          }
          notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
          context,
          0,
          openIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
          .setSmallIcon(R.drawable.school_logo)
          .setContentTitle("ЛБХБ Турсунзода • Давомот")
          .setContentText("Субҳ ба хайр! Вақти қайди давомоти имрӯзаи хонандагон расид.")
          .setPriority(NotificationCompat.PRIORITY_DEFAULT)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true)
          .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
