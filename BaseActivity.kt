package app.luma.torch

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

open class BaseActivity : Activity() {
    val isDark: Boolean get() = when (Torch.theme) {
        1 -> false
        2 -> true
        else -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    val bg get() = if (isDark) 0xFF0B0B0D.toInt() else 0xFFF2F3F5.toInt()
    val card get() = if (isDark) 0xFF17171A.toInt() else Color.WHITE
    val chip get() = if (isDark) 0xFF26262B.toInt() else 0xFFECEDF0.toInt()
    val fg get() = if (isDark) Color.WHITE else Color.BLACK
    val dim get() = if (isDark) 0xFF9A9AA3.toInt() else 0xFF6B6B74.toInt()
    val acc get() = if (isDark) 0xFF7CF7D4.toInt() else 0xFF00A383.toInt()
    val onAcc get() = if (isDark) Color.BLACK else Color.WHITE
    val warn = 0xFFFF5A5F.toInt()
    private var built = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Torch.init(this)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        window.statusBarColor = bg; window.navigationBarColor = bg
        window.decorView.systemUiVisibility = if (isDark) 0 else 0x2010
        built = isDark
    }

    override fun onStart() { super.onStart(); if (isDark != built) recreate() }
    fun dlg() = AlertDialog.Builder(this, if (isDark) android.R.style.Theme_Material_Dialog_Alert else android.R.style.Theme_Material_Light_Dialog_Alert)

    fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    fun shape(c: Int, r: Int) = GradientDrawable().apply { cornerRadius = dp(r).toFloat(); setColor(c) }
    fun icon(res: Int, c: Int): Drawable = getDrawable(res)!!.mutate().apply { setTint(c) }

    fun tv(t: String, size: Float = 15f, click: () -> Unit) = TextView(this).apply {
        text = t; textSize = size; gravity = Gravity.CENTER; setPadding(0, dp(13), 0, dp(13))
        setTextColor(fg); setOnClickListener { click() }
    }
    fun style(v: TextView, sel: Boolean) {
        v.setTextColor(if (sel) onAcc else fg)
        v.background = shape(if (sel) acc else chip, 12)
    }
    fun row(items: List<TextView>) = LinearLayout(this).apply {
        items.forEach { addView(it, LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(3), 0, dp(3), 0) }) }
    }
    fun group(names: List<String>, sel: () -> Int, pick: (Int) -> Unit): LinearLayout {
        val cs = mutableListOf<TextView>()
        fun paint() = cs.forEachIndexed { i, c -> style(c, sel() == i) }
        names.forEachIndexed { i, n -> cs += tv(n, 14f) { pick(i); paint() } }
        paint()
        return row(cs)
    }
    fun section(title: String, vararg v: View) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; background = shape(card, 20)
        setPadding(dp(16), dp(14), dp(16), dp(16))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        addView(TextView(this@BaseActivity).apply { text = title; setTextColor(dim); textSize = 13f; setPadding(dp(3), 0, 0, dp(10)) })
        v.forEach { addView(it) }
    }
    fun header(title: String, end: View? = null) = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }
        addView(TextView(this@BaseActivity).apply { text = title; textSize = 26f; setTextColor(fg); typeface = Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0, -2, 1f))
        end?.let { addView(it) }
    }
    fun page(vararg v: View) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(44), dp(20), dp(24)); v.forEach { addView(it) }
        }
        setContentView(ScrollView(this).apply { setBackgroundColor(bg); addView(root) })
    }
}
