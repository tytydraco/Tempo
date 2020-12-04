package com.draco.tempo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var buttonContainer: LinearLayout

    /* Merge two colors by a ratio [0,1] */
    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    private fun setAnimationSpeed(scale: Float) {
        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            scale
        )

        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            scale
        )

        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            scale
        )
    }

    private fun hasPermissions(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions() {
        if (hasPermissions())
            return

        val adbCommand = "pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.missingPermissions))
            .setMessage(getString(R.string.adb_tutorial) + "adb shell $adbCommand")
            .setPositiveButton(getString(R.string.buttonCheckAgain), null)
            .setNeutralButton(getString(R.string.buttonSetupADB), null)
            .setNegativeButton(getString(R.string.buttonUseRoot), null)
            .setCancelable(false)
            .show()

        dialog.setOnShowListener {
            /* We don't dismiss on Check Again unless we actually have the permission */
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                if (hasPermissions())
                    dialog.dismiss()
            }

            /* Open tutorial but do not dismiss until user presses Check Again */
            val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                val uri = Uri.parse("https://www.xda-developers.com/install-adb-windows-macos-linux/")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }

            /* Try using root permissions */
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setOnClickListener {
                try {
                    ProcessBuilder("su", "-c", adbCommand).start()
                    if (hasPermissions())
                        dialog.dismiss()
                } catch (_: Exception) {}
            }
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonContainer = findViewById(R.id.buttons)

        /* Setup scales for each button */
        for (rawSpeed in 0 .. 100) {
            val speed = rawSpeed / 100f
            val button = MaterialButton(ContextThemeWrapper(this, R.style.Theme_Tempo_Button))

            val animation = AnimationUtils.loadAnimation(this, R.anim.shake)
            animation.scaleCurrentDuration(speed)
            button.setOnClickListener {
                setAnimationSpeed(speed)
                if (!animation.hasStarted() || animation.hasEnded())
                    it.startAnimation(animation)
            }
            button.text = "$rawSpeed%"
            button.setBackgroundColor(
                blendColors(
                    getColor(R.color.buttonMin),
                    getColor(R.color.buttonMax),
                    speed
                )
            )
            buttonContainer.addView(button)
        }

        /* Ensure we have the secure settings write permission */
        checkPermissions()
    }
}
