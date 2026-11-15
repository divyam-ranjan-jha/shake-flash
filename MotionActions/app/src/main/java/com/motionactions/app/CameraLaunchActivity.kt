package com.motionactions.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class CameraLaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            startActivity(Intent("android.media.action.IMAGE_CAPTURE"))
        } finally {
            finish()
        }
    }
}
