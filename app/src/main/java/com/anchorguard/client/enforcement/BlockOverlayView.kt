package com.anchorguard.client.enforcement

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.anchorguard.client.MainActivity
import com.anchorguard.client.R

object BlockOverlayView {
    @Volatile
    private var view: View? = null
    private val lock = Any()

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun show(context: Context, blockedPackage: String?) {
        val appContext = context.applicationContext
        if (!canDrawOverlays(appContext)) return
        synchronized(lock) {
            val wm = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val existing = view
            if (existing != null) {
                update(existing, appContext, blockedPackage)
                return
            }
            val inflater = LayoutInflater.from(appContext)
            val v = inflater.inflate(R.layout.overlay_block, null)
            update(v, appContext, blockedPackage)
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.CENTER }
            try {
                wm.addView(v, params)
                view = v
            } catch (_: Throwable) {
                view = null
            }
        }
    }

    private fun update(v: View, context: Context, blockedPackage: String?) {
        val nameView = v.findViewById<TextView>(R.id.blocked_app_name)
        val timerView = v.findViewById<TextView>(R.id.blocked_timer)
        val unlock = v.findViewById<Button>(R.id.blocked_unlock_button)
        val appName = resolveLabel(context, blockedPackage)
        nameView.text = if (appName != null) "$appName is not available right now." else context.getString(R.string.overlay_blocked_app_default)
        timerView.text = "" // populated when schedule/timer policy available
        unlock.setOnClickListener {
            hide(context)
            val i = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_OPEN_PARENT_GATE, true)
            }
            context.startActivity(i)
        }
    }

    private fun resolveLabel(context: Context, pkg: String?): String? {
        if (pkg.isNullOrBlank()) return null
        return try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(ai)?.toString()
        } catch (_: Throwable) {
            pkg
        }
    }

    fun hide(context: Context) {
        val appContext = context.applicationContext
        synchronized(lock) {
            val v = view ?: return
            val wm = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            try {
                wm.removeView(v)
            } catch (_: Throwable) {
            } finally {
                view = null
            }
        }
    }

    const val EXTRA_OPEN_PARENT_GATE = "extra_open_parent_gate"
}
