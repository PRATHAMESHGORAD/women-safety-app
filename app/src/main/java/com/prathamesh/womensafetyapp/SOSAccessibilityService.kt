package com.prathamesh.womensafetyapp

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class SOSAccessibilityService : AccessibilityService() {
    private var lastPressTime: Long = 0
    private var pressCount = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.action == KeyEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPressTime <= 1000) {
                pressCount++
            } else {
                pressCount = 1
            }
            lastPressTime = currentTime

            if (pressCount >= 3) {
                pressCount = 0
                Toast.makeText(this, "🚨 SOS Triggered (Volume triple press)", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, SOSService::class.java)
                startService(intent)


            }
        }
        return true // Must consume event
    }
}
