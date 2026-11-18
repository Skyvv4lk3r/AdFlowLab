package com.example.adflowlab

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

/** Read-only observation of UI labels and bounds. Never dispatches gestures or clicks. */
class LabAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var running = false
        @Volatile var instance: LabAccessibilityService? = null
        @Volatile var latest = "Waiting for accessibility events"
        @Volatile var log = ""
        fun record(message: String) {
            latest = message
            log = (log + "\n" + System.currentTimeMillis() + " " + message).takeLast(30000)
            Log.i("AdFlowLab", message)
        }
    }
    private var lastSnapshot = ""
    override fun onServiceConnected() { instance = this; record("Accessibility service connected") }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!running || event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        val root = rootInActiveWindow ?: return
        val rootPkg = root.packageName?.toString() ?: pkg
        val items = mutableListOf<String>()
        collect(root, items, 0)
        val text = items.joinToString(" | ").take(2500)
        val progress = Regex("\\b(\\d{1,2})\\s*/\\s*16\\b").find(text)?.value
        val classification = when {
            text.contains("unable to load", true) -> "LOAD_ERROR"
            text.contains("playing next ad", true) -> "COUNTDOWN"
            text.contains("unlocked", true) -> "UNLOCK_CONFIRMED"
            text.contains("close", true) || text.contains("next", true) || text.contains("skip", true) -> "CONTROL_VISIBLE"
            else -> "OBSERVING"
        }
        val snapshot = "$rootPkg $classification progress=${progress ?: "unknown"} $text"
        if (snapshot != lastSnapshot) {
            lastSnapshot = snapshot
            record(snapshot)
        }
    }
    private fun collect(node: AccessibilityNodeInfo, out: MutableList<String>, depth: Int) {
        if (depth > 18 || out.size >= 60) return
        val label = (node.text ?: node.contentDescription)?.toString()?.trim().orEmpty()
        if (label.isNotBlank()) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            out.add("${label.take(90)}@${bounds.left},${bounds.top},${bounds.right},${bounds.bottom}${if (node.isClickable) "[clickable]" else ""}")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collect(child, out, depth + 1)
        }
    }
    override fun onInterrupt() { record("Accessibility interrupted") }
    override fun onDestroy() { instance = null; running = false; super.onDestroy() }
}
