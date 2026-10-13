package app.luma.torch

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class CameraButtonService : AccessibilityService() {
    private var lastUp = 0L
    private var fired = false

    override fun onServiceConnected() { Torch.init(this) }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(e: KeyEvent): Boolean {
        if (e.keyCode != KeyEvent.KEYCODE_CAMERA) return false
        if (e.action == KeyEvent.ACTION_DOWN) {
            if (e.repeatCount == 1 && Torch.longAct != 0) { fired = true; act(Torch.longAct, Torch.longPkg) }
            return fired
        }
        val t = SystemClock.elapsedRealtime()
        if (fired) { fired = false; lastUp = 0; return true }
        if (Torch.dblAct != 0 && t - lastUp < 400) { lastUp = 0; act(Torch.dblAct, Torch.dblPkg); return true }
        lastUp = t
        return false
    }

    private fun act(a: Int, pkg: String) {
        when (a) {
            1 -> Torch.set(!Torch.on)
            2 -> packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
    }
}
