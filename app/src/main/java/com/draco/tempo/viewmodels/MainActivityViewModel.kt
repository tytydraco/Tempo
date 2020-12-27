package com.draco.tempo.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Color
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.draco.tempo.R

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private val buttonMinColor = context.getColor(R.color.buttonMin)
    private val buttonMaxColor = context.getColor(R.color.buttonMax)

    /* Merge two colors by a ratio [0,1] */
    fun blendColors(ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(buttonMinColor) * ratio + Color.red(buttonMaxColor) * inverseRatio
        val g = Color.green(buttonMinColor) * ratio + Color.green(buttonMaxColor) * inverseRatio
        val b = Color.blue(buttonMinColor) * ratio + Color.blue(buttonMaxColor) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    fun formatSpeedText(speed: Int) = "$speed%"

    fun hasPermissions(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    fun setAnimationSpeed(scale: Float) {
        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            scale
        )

        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            scale
        )

        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            scale
        )
    }
}