package app.luma.torch

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

class FlashActivity : BaseActivity() {
    private lateinit var dial: ImageView
    private lateinit var halo: View
    private lateinit var sosBtn: TextView
    private lateinit var status: TextView
    private val refresh = { ui() }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        dial = ImageView(this).apply {
            setImageResource(R.drawable.ic_power); setPadding(dp(62), dp(62), dp(62), dp(62))
            setOnClickListener { Torch.set(!Torch.on) }
        }
        halo = View(this)
        val hero = FrameLayout(this).apply {
            addView(halo, FrameLayout.LayoutParams(dp(260), dp(260), Gravity.CENTER))
            addView(dial, FrameLayout.LayoutParams(dp(196), dp(196), Gravity.CENTER))
            layoutParams = LinearLayout.LayoutParams(-1, dp(270))
        }
        status = TextView(this).apply { gravity = Gravity.CENTER; textSize = 18f; setTextColor(fg); setPadding(0, dp(4), 0, dp(20)) }
        sosBtn = tv("מצב מצוקה SOS", 16f) { Torch.sos(!Torch.sos) }
        sosBtn.compoundDrawablePadding = dp(10)
        sosBtn.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        val times = intArrayOf(0, 30, 60, 300, 600)
        val sw = Switch(this).apply {
            text = "הדלקה וכיבוי בניעור"; setTextColor(fg); isChecked = Torch.shake
            setOnCheckedChangeListener { _, c ->
                Torch.shake = c
                val i = Intent(this@FlashActivity, FlashService::class.java)
                if (c) startForegroundService(i) else stopService(i)
            }
        }
        val snd = Switch(this).apply {
            text = "צליל קצר בהדלקה בניעור"; setTextColor(fg); isChecked = Torch.sound; setPadding(0, dp(12), 0, 0)
            setOnCheckedChangeListener { _, c -> Torch.sound = c }
        }
        val addTile = tv("הוספת כפתור פנס לסרגל ההגדרות המהירות") { addTile() }
        style(addTile, false)
        val about = TextView(this).apply { text = "Luma גרסה 1.0\nפנס פשוט ומהיר. הכול נשאר במכשיר שלך."; setTextColor(fg) }
        page(
            header("פנס"), hero, status,
            section("בהירות", group(listOf("נמוך", "בינוני", "גבוה", "מקסימום"), { Torch.level - 1 }) { Torch.level = it + 1; Torch.relevel() }),
            section("כיבוי אוטומטי", group(times.map { if (it == 0) "ללא" else if (it < 60) "$it שנ׳" else if (it == 60) "דקה" else "${it / 60} דק׳" }, { times.indexOf(Torch.timer) }) { Torch.timer = times[it] }),
            sosBtn,
            section("תנועה", sw, snd),
            section("סרגל ההגדרות המהירות", addTile),
            section("ערכת נושא", group(listOf("אוטומטי", "בהיר", "כהה"), { Torch.theme }) { Torch.theme = it; recreate() }),
            section("אודות", about)
        )
        ui()
    }

    private fun addTile() {
        if (Build.VERSION.SDK_INT >= 33) {
            getSystemService(StatusBarManager::class.java).requestAddTileService(
                ComponentName(this, FlashTile::class.java), "פנס",
                Icon.createWithResource(this, R.drawable.ic_flash), mainExecutor) { }
        } else {
            dlg().setMessage("החלק מטה מראש המסך פעמיים, לחץ על סמל העריכה (עיפרון) וגרור את האריח \"פנס\" לסרגל.").setPositiveButton("הבנתי", null).show()
        }
    }

    override fun onStart() { super.onStart(); Torch.listeners += refresh; ui() }
    override fun onStop() { super.onStop(); Torch.listeners -= refresh }

    private fun ui() {
        val on = Torch.on
        dial.setColorFilter(if (on) onAcc else acc)
        dial.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(if (on) acc else card); setStroke(dp(2), if (on) acc else chip)
        }
        halo.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(if (on) (acc and 0x00FFFFFF) or 0x33000000 else Color.TRANSPARENT)
        }
        sosBtn.setTextColor(if (Torch.sos) Color.WHITE else warn)
        sosBtn.background = shape(if (Torch.sos) warn else card, 20)
        sosBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(icon(R.drawable.ic_sos, if (Torch.sos) Color.WHITE else warn), null, null, null)
        status.text = when {
            Torch.busy -> "המצלמה בשימוש באפליקציה אחרת"
            Torch.sos -> "SOS פעיל"
            on -> "הפנס דלוק"
            else -> "הפנס כבוי"
        }
    }
}
