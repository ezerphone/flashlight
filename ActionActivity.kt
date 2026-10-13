package app.luma.torch

import android.app.Activity
import android.os.Bundle

class ActionActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Torch.init(this)
        when (intent.action) {
            "app.luma.torch.TORCH" -> Torch.set(!Torch.on)
            "app.luma.torch.SOS" -> Torch.sos(!Torch.sos)
        }
        finish()
    }
}
