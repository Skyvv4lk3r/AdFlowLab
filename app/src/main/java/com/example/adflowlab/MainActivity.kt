package com.example.adflowlab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** On-device read-only accessibility monitor. No automated interaction with third-party apps. */
class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var status: TextView
    private lateinit var events: TextView
    private val update = object : Runnable {
        override fun run() {
            status.text = "Monitoring: ${if (LabAccessibilityService.running) "ON" else "OFF"}\n${LabAccessibilityService.latest}"
            events.text = LabAccessibilityService.log.takeLast(10000)
            handler.postDelayed(this, 1000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 30, 24, 24) }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        root.addView(TextView(this).apply { text = "AdFlow Lab — Live UI Observer"; textSize = 22f })
        root.addView(TextView(this).apply { text = "Read-only diagnostics. Screen labels and coordinates may contain private information. Export logs only when needed." })
        root.addView(Button(this).apply { text = "Enable Accessibility Service"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        root.addView(Button(this).apply { text = "Start monitoring"; setOnClickListener { LabAccessibilityService.running = true; LabAccessibilityService.record("Monitoring started") } })
        root.addView(Button(this).apply { text = "STOP monitoring"; setOnClickListener { LabAccessibilityService.running = false; LabAccessibilityService.record("Monitoring stopped") } })
        root.addView(Button(this).apply { text = "Share diagnostic log"; setOnClickListener {
            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, LabAccessibilityService.log) }
            startActivity(Intent.createChooser(send, "Share observation log"))
        } })
        root.addView(Button(this).apply { text = "Clear log"; setOnClickListener { LabAccessibilityService.log = "" } })
        status = TextView(this).apply { textSize = 16f }; root.addView(status)
        events = TextView(this).apply { textSize = 12f; setTextIsSelectable(true) }; root.addView(events)
    }
    override fun onResume() { super.onResume(); handler.removeCallbacks(update); handler.post(update) }
    override fun onPause() { handler.removeCallbacks(update); super.onPause() }
}
