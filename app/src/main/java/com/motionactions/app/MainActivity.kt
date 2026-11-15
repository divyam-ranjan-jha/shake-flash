package com.motionactions.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.ViewGroup

class MainActivity : Activity() {
    private val requestCode = 41

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 56, 48, 48)
        }
        layout.addView(TextView(this).apply {
            text = "Motion Actions\n\nMVP: Double Chop → Flashlight\nDouble Twist → Camera notification\n\nKeep the foreground notification visible while monitoring is enabled. Screen-off behaviour must be tested on your Galaxy M35."
            textSize = 18f
        }, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layout.addView(Button(this).apply {
            text = "Enable gesture detection"
            setOnClickListener { requestNeededPermissionsAndStart() }
        })
        layout.addView(Button(this).apply {
            text = "Stop gesture detection"
            setOnClickListener { stopService(Intent(this@MainActivity, MotionService::class.java)) }
        })
        layout.addView(Button(this).apply {
            text = "Open notification settings"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= 26) {
                    startActivity(Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName))
                }
            }
        })
        setContentView(layout)
    }

    private fun requestNeededPermissionsAndStart() {
        val needed = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) needed += Manifest.permission.CAMERA
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) needed += Manifest.permission.POST_NOTIFICATIONS
        if (needed.isNotEmpty()) requestPermissions(needed.toTypedArray(), requestCode) else startMotionService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) startMotionService()
    }

    private fun startMotionService() {
        val intent = Intent(this, MotionService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }
}
