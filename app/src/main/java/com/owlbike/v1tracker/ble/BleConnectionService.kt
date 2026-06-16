package com.owlbike.v1tracker.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.owlbike.v1tracker.MainActivity
import com.owlbike.v1tracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BleConnectionService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var manager: BleSessionManager
    private var foregroundStarted = false

    override fun onCreate() {
        super.onCreate()
        manager = BleSessionManager.get(applicationContext)
        createNotificationChannel()
        serviceScope.launch {
            manager.connection.collect { connection ->
                if (!manager.keepAliveActive && !connection.isConnected && !connection.isConnecting) {
                    stopSelf()
                } else {
                    ensureForeground(connection)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISCONNECT) {
            manager.disconnect()
            stopSelf()
            return START_NOT_STICKY
        }
        if (!manager.keepAliveActive) {
            stopSelf()
            return START_NOT_STICKY
        }
        ensureForeground(manager.connection.value)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun ensureForeground(connection: BleConnectionState) {
        val notification = buildNotification(connection)
        if (!foregroundStarted) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                } else {
                    0
                },
            )
            foregroundStarted = true
        } else {
            runCatching {
                getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun buildNotification(connection: BleConnectionState): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val disconnectIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, BleConnectionService::class.java).setAction(ACTION_DISCONNECT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val device = connection.deviceName ?: connection.deviceAddress ?: getString(R.string.app_name)
        val status = when {
            connection.isConnected -> getString(R.string.connected)
            connection.isConnecting -> connection.status
            else -> getString(R.string.not_connected)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.ble_notification_title))
            .setContentText(getString(R.string.ble_notification_text, device, status))
            .setContentIntent(openIntent)
            .setOngoing(connection.isConnected || connection.isConnecting)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.disconnect),
                disconnectIntent,
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.ble_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.owlbike.v1tracker.ble.action.START_KEEP_ALIVE"
        const val ACTION_DISCONNECT = "com.owlbike.v1tracker.ble.action.DISCONNECT"
        private const val CHANNEL_ID = "owl_bike_ble_connection"
        private const val NOTIFICATION_ID = 1001
    }
}
