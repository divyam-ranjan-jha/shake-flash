package com.motionactions.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

class MotionService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var gyro: Sensor? = null
    private var torchOn = false
    private var lastChopPeak = 0L
    private var chopSign = 0
    private var chopCount = 0
    private var lastTwistPeak = 0L
    private var twistSign = 0
    private var twistCount = 0
    private var lastAction = 0L
    private var torchId: String? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Listening for gestures"))
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accel?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        torchId = try {
            val cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cm.cameraIdList.firstOrNull { id -> cm.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
        } catch (e: Exception) { null }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = SystemClock.elapsedRealtime()
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                val axis = listOf(x, y, z).maxByOrNull { abs(it) } ?: 0f
                // Prototype chop detector: two strong, alternating linear-acceleration peaks.
                if (abs(axis) > 13.5f) {
                    val sign = if (axis > 0) 1 else -1
                    if (sign != chopSign && now - lastChopPeak in 90..850) chopCount++
                    else if (now - lastChopPeak > 850) chopCount = 1
                    chopSign = sign
                    lastChopPeak = now
                    if (chopCount >= 2 && now - lastAction > 1800) {
                        toggleTorch(); lastAction = now; chopCount = 0
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                val axis = listOf(x, y, z).maxByOrNull { abs(it) } ?: 0f
                // Prototype twist detector: two strong rotations in opposite directions.
                if (abs(axis) > 3.2f) {
                    val sign = if (axis > 0) 1 else -1
                    if (sign != twistSign && now - lastTwistPeak in 100..900) twistCount++
                    else if (now - lastTwistPeak > 900) twistCount = 1
                    twistSign = sign
                    lastTwistPeak = now
                    if (twistCount >= 2 && now - lastAction > 1800) {
                        postCameraNotification(); lastAction = now; twistCount = 0
                    }
                }
            }
        }
    }

    private fun toggleTorch() {
        val id = torchId ?: return
        try {
            torchOn = !torchOn
            (getSystemService(Context.CAMERA_SERVICE) as CameraManager).setTorchMode(id, torchOn)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, buildNotification(if (torchOn) "Chop detected · Flashlight ON" else "Chop detected · Flashlight OFF"))
        } catch (e: Exception) { Log.e("MotionActions", "Torch toggle failed", e) }
    }

    private fun postCameraNotification() {
        val intent = Intent(this, CameraLaunchActivity::class.java)
        val pi = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = notificationBuilder()
        notification.setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Double twist detected")
            .setContentText("Tap to open the camera")
            .setContentIntent(pi)
            .setAutoCancel(true)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(CAMERA_NOTIFICATION_ID, notification.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Motion gesture detection", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    @Suppress("DEPRECATION")
    private fun notificationBuilder(): Notification.Builder =
        if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this)

    private fun buildNotification(message: String): Notification {
        val b = notificationBuilder()
        return b.setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Motion Actions active")
            .setContentText(message)
            .setOngoing(true)
            .build()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { sensorManager.unregisterListener(this); super.onDestroy() }

    companion object {
        private const val CHANNEL_ID = "motion_actions"
        private const val NOTIFICATION_ID = 1001
        private const val CAMERA_NOTIFICATION_ID = 1002
    }
}
