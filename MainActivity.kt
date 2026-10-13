package app.luma.torch

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.widget.*

class MainActivity : BaseActivity() {
    private fun hasCameraButton() = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_CAMERA) ||
        InputDevice.getDeviceIds().any { InputDevice.getDevice(it)?.hasKeys(KeyEvent.KEYCODE_CAMERA)?.firstOrNull() == true }

    private fun big(res: Int, t: String, s: String, go: Class<*>) = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL; background = shape(card, 22); setPadding(dp(20), dp(26), dp(20), dp(26))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        addView(ImageView(this@MainActivity).apply { setImageDrawable(icon(res, acc)) },
            LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginEnd = dp(16) })
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply { text = t; textSize = 20f; setTextColor(fg); typeface = Typeface.DEFAULT_BOLD })
            addView(TextView(this@MainActivity).apply { text = s; textSize = 13f; setTextColor(dim) })
        }, LinearLayout.LayoutParams(0, -2, 1f))
        setOnClickListener { startActivity(Intent(this@MainActivity, go)) }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        if (Torch.shake) startForegroundService(Intent(this, FlashService::class.java))
        val items = mutableListOf<android.view.View>(
            header("Luma"),
            big(R.drawable.ic_flash, "פנס", "הדלקה, בהירות, SOS, ניעור והגדרות", FlashActivity::class.java))
        if (hasCameraButton()) items += big(R.drawable.ic_camera, "כפתור המצלמה", "מה יקרה בלחיצה ארוכה או כפולה", CameraButtonActivity::class.java)
        page(*items.toTypedArray())
    }
}
