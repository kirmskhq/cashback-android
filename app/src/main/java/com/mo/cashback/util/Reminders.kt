package com.mo.cashback.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mo.cashback.MainActivity
import com.mo.cashback.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

object Reminders {
    const val CHANNEL_ID = "monthly_reminder"
    private const val REQUEST_CODE = 7301
    private const val REMINDER_HOUR = 10
    private const val REMINDER_DAY = 30

    /** Create the notification channel (idempotent). Safe to call on every startup. */
    fun ensureChannel(ctx: Context) {
        val mgr = ctx.getSystemService(NotificationManager::class.java) ?: return
        val ch = NotificationChannel(
            CHANNEL_ID,
            ctx.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = ctx.getString(R.string.reminder_channel_desc)
        }
        mgr.createNotificationChannel(ch)
    }

    /** Schedule the next "pick your cashback categories" reminder. */
    fun scheduleNext(ctx: Context) {
        val alarm = ctx.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(ctx, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val whenMillis = nextFireTimeMillis()
        // Inexact alarm — needs no special permission and is fine for a monthly nudge.
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pi)
    }

    /** Compute the next 30th-of-month (or last day of February) at 10:00 local time. */
    internal fun nextFireTimeMillis(now: LocalDateTime = LocalDateTime.now()): Long {
        var ym = YearMonth.of(now.year, now.monthValue)
        var day = minOf(REMINDER_DAY, ym.lengthOfMonth())
        var target = LocalDateTime.of(LocalDate.of(ym.year, ym.month, day), LocalTime.of(REMINDER_HOUR, 0))
        if (!target.isAfter(now)) {
            ym = ym.plusMonths(1)
            day = minOf(REMINDER_DAY, ym.lengthOfMonth())
            target = LocalDateTime.of(LocalDate.of(ym.year, ym.month, day), LocalTime.of(REMINDER_HOUR, 0))
        }
        return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Build & post notification
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, Reminders.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_body))
            .setContentIntent(openPi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Post — silently no-op if POST_NOTIFICATIONS not granted on API 33+
        val mgr = context.getSystemService(NotificationManager::class.java)
        try {
            mgr?.notify(7301, builder.build())
        } catch (_: SecurityException) { /* user revoked permission */ }

        // Re-arm for next month
        Reminders.scheduleNext(context)
    }
}
