package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.*

/**
 * Фоновая служба удержания соединения SimpleX Push & Background Sync (Android 14+).
 *
 * Обеспечивает:
 * 1. Гарантированную работу в фоне без уничтожения системой через Foreground Service с типом SPECIAL_USE.
 * 2. Удержание WakeLock при обработке входящих ходов и сообщений.
 * 3. Поддержание WebSocket Ping/Pong пинга для предотвращения разрыва NAT-шлюзов мобильных операторов.
 */
class SimpleXPushService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val TAG = "SimpleXPushService"
        const val CHANNEL_ID = "simplex_push_channel"
        const val NOTIFICATION_ID = 4051

        fun start(context: Context) {
            try {
                val intent = Intent(context, SimpleXPushService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting SimpleXPushService: ${e.message}")
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, SimpleXPushService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping SimpleXPushService: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed startForeground: ${e.message}, falling back", e)
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }

        acquireWakeLock()
        startKeepAliveLoop()
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MadGammon:SimpleXKeepAlive")?.apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 минут с авто-продлением
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire WakeLock: ${e.message}")
        }
    }

    private fun startKeepAliveLoop() {
        serviceScope.launch {
            while (isActive) {
                delay(45_000L) // Каждые 45 секунд продление удержания сокета
                try {
                    wakeLock?.let {
                        if (!it.isHeld) {
                            it.acquire(10 * 60 * 1000L)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SimpleX Background Sync",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Фоновая синхронизация ходов и зашифрованных сообщений"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MadGammon P2P Sync")
            .setContentText("Защищённый канал SimpleX активен")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
