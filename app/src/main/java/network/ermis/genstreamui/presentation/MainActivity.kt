package network.ermis.genstreamui.presentation

import dagger.hilt.android.AndroidEntryPoint

import network.ermis.genstreamui.R

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Hide system bars and enable immersive mode
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val root = findViewById<View>(android.R.id.content)
            val target = findTargetView(root, ev.rawX, ev.rawY)
            if (target != null) {
                target.isFocusableInTouchMode = false
            }
        }

        val result = super.dispatchTouchEvent(ev)

        if (ev.action == MotionEvent.ACTION_UP) {
            val root = findViewById<View>(android.R.id.content)
            val target = findTargetView(root, ev.rawX, ev.rawY)
            if (target != null && target.isFocusable) {
                val currentFocused = currentFocus
                if (currentFocused != null && currentFocused != target) {
                    currentFocused.isFocusableInTouchMode = false
                }
                
                target.isFocusableInTouchMode = true
                target.requestFocus()
            }
        }
        return result
    }

    private fun findTargetView(root: View, x: Float, y: Float): View? {
        if (root.visibility != View.VISIBLE) return null
        
        val location = IntArray(2)
        root.getLocationOnScreen(location)
        val rect = android.graphics.Rect(location[0], location[1], location[0] + root.width, location[1] + root.height)
        
        if (!rect.contains(x.toInt(), y.toInt())) return null

        if (root is ViewGroup) {
            for (i in root.childCount - 1 downTo 0) {
                val child = root.getChildAt(i)
                val target = findTargetView(child, x, y)
                if (target != null) return target
            }
        }
        
        if (root.isClickable) {
            return root
        }
        return null
    }
}