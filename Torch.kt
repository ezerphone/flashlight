package app.luma.torch

import android.content.Context
import android.content.SharedPreferences
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper

object Torch {
    var on = false
    var sos = false
    var busy = false // camera used by another app
    var level = 4 // 1..4
    val listeners = mutableListOf<() -> Unit>()
    lateinit var sp: SharedPreferences
    private var cm: CameraManager? = null
    private var id: String? = null
    private var lastSet = 0L
    private val h = Handler(Looper.getMainLooper())
    private val pat = longArrayOf(200,200,200,200,200,600, 600,200,600,200,600,600, 200,200,200,200,200,1500)

    var timer: Int get() = sp.getInt("t", 0); set(v) = sp.edit().putInt("t", v).apply()
    var shake: Boolean get() = sp.getBoolean("s", false); set(v) = sp.edit().putBoolean("s", v).apply()
    var sound: Boolean get() = sp.getBoolean("snd", true); set(v) = sp.edit().putBoolean("snd", v).apply()
    var theme: Int get() = sp.getInt("th", 0); set(v) = sp.edit().putInt("th", v).apply() // 0 auto,1 light,2 dark
    var longAct: Int get() = sp.getInt("la", 1); set(v) = sp.edit().putInt("la", v).apply() // 0 none,1 torch,2 home
    var dblAct: Int get() = sp.getInt("da", 0); set(v) = sp.edit().putInt("da", v).apply()
    var longPkg: String get() = sp.getString("lp", "") ?: ""; set(v) = sp.edit().putString("lp", v).apply()
    var dblPkg: String get() = sp.getString("dp", "") ?: ""; set(v) = sp.edit().putString("dp", v).apply()

    fun init(c: Context) {
        if (cm != null) return
        sp = c.applicationContext.getSharedPreferences("luma", 0)
        val m = c.getSystemService(CameraManager::class.java)
        cm = m
        id = m.cameraIdList.firstOrNull { m.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
        m.registerTorchCallback(object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(c: String, e: Boolean) {
                if (c != id) return
                busy = false
                if (!sos && e != on && System.currentTimeMillis() - lastSet > 700) on = e
                emit()
            }
            override fun onTorchModeUnavailable(c: String) {
                if (c != id) return
                busy = true; h.removeCallbacksAndMessages(null); on = false; sos = false; emit()
            }
        }, h)
    }

    private fun hw(v: Boolean) {
        val i = id ?: return
        val m = cm ?: return
        try {
            if (v && Build.VERSION.SDK_INT >= 33) {
                val max = m.getCameraCharacteristics(i).get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                if (max > 1) { m.turnOnTorchWithStrengthLevel(i, (max * level / 4).coerceAtLeast(1)); return }
            }
            m.setTorchMode(i, v)
        } catch (_: Exception) {}
    }

    private fun emit() = listeners.toList().forEach { it() }

    fun set(v: Boolean) {
        if (v && busy) { emit(); return }
        h.removeCallbacksAndMessages(null)
        lastSet = System.currentTimeMillis()
        sos = false; on = v; hw(v)
        if (v && timer > 0) h.postDelayed({ set(false) }, timer * 1000L)
        emit()
    }

    fun relevel() { if (on && !sos) hw(true) }

    fun sos(v: Boolean) {
        if (!v) { set(false); return }
        set(true)
        if (!on) return
        sos = true; emit()
        var i = 0
        val r = object : Runnable {
            override fun run() {
                if (!sos) return
                hw(i % 2 == 0); h.postDelayed(this, pat[i % pat.size]); i++
            }
        }
        h.post(r)
    }
}
