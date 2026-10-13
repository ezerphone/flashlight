package app.luma.torch

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*

class CameraButtonActivity : BaseActivity() {
    private fun appName(p: String) = try { packageManager.getApplicationInfo(p, 0).loadLabel(packageManager).toString() } catch (_: Exception) { "לא נבחרה" }

    private fun choose(cb: (String) -> Unit) {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(i, 0)
            .map { it.loadLabel(packageManager).toString() to it.activityInfo.packageName }
            .distinctBy { it.second }.sortedBy { it.first.lowercase() }
        dlg().setTitle("בחירת אפליקציה").setItems(apps.map { it.first }.toTypedArray()) { _, k -> cb(apps[k].second) }.show()
    }

    private fun block(title: String, act: () -> Int, setAct: (Int) -> Unit, pkg: () -> String, setPkg: (String) -> Unit): View {
        val lbl = TextView(this).apply { setTextColor(dim); textSize = 13f; setPadding(dp(3), dp(10), 0, 0) }
        fun upd() {
            lbl.visibility = if (act() == 2) View.VISIBLE else View.GONE
            lbl.text = "אפליקציה: " + appName(pkg()) + " (לחץ לשינוי)"
        }
        lbl.setOnClickListener { choose { setPkg(it); upd() } }
        val g = group(listOf("ללא", "פנס", "פתיחת אפליקציה"), act) { i ->
            setAct(i)
            if (i == 2) choose { setPkg(it); upd() }
            upd()
        }
        upd()
        return section(title, g, lbl)
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val a11y = tv("הפעלת שירות כפתור המצלמה") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        style(a11y, false)
        val hint = TextView(this).apply {
            text = "כדי שזה יעבוד צריך להפעיל את השירות Luma בהגדרות הנגישות. השירות מזהה רק את כפתור המצלמה, לא קורא תוכן מהמסך ולא אוסף מידע. עובד כשהמסך דלוק."
            setTextColor(dim); textSize = 12f; setPadding(dp(3), dp(10), 0, 0)
        }
        page(
            header("כפתור המצלמה"),
            block("לחיצה ארוכה", { Torch.longAct }, { Torch.longAct = it }, { Torch.longPkg }, { Torch.longPkg = it }),
            block("לחיצה כפולה", { Torch.dblAct }, { Torch.dblAct = it }, { Torch.dblPkg }, { Torch.dblPkg = it }),
            section("שירות נגישות", a11y, hint)
        )
    }
}
