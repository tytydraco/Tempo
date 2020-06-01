package com.draco.tempo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    private val adbCommand = "pm grant ${BuildConfig.APPLICATION_ID} android.permission.WRITE_SECURE_SETTINGS"

    private lateinit var ancient: Button
    private lateinit var slow: Button
    private lateinit var relaxed: Button
    private lateinit var loose: Button
    private lateinit var default: Button
    private lateinit var hasty: Button
    private lateinit var snappy: Button
    private lateinit var snappier: Button
    private lateinit var fast: Button
    private lateinit var faster: Button
    private lateinit var instant: Button

    /* Window, Transition, Animator */
    private val speedValueList = listOf(
        2.0,
        1.5,
        1.2,
        1.1,
        1.0,
        0.9,
        0.7,
        0.6,
        0.5,
        0.2,
        0.0
    )

    private lateinit var speedButtonList: List<Button>

    private val buttonColorMin = Color.parseColor("#7b1fa2")
    private val buttonColorMax = Color.parseColor("#d32f2f")

    /* Merge two colors by a ratio [0,1] */
    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    private fun setAnimationSpeed(scale: Double) {
        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            scale.toFloat()
        )

        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            scale.toFloat()
        )

        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            scale.toFloat()
        )
    }

    private fun hasPermissions(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions() {
        if (hasPermissions())
            return

        val dialog = AlertDialog.Builder(this)
            .setTitle("Missing Permissions")
            .setMessage(getString(R.string.adb_tutorial) + "adb shell $adbCommand")
            .setPositiveButton("Check Again", null)
            .setNeutralButton("Setup ADB", null)
            .setCancelable(false)
            .create()

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
        }

        dialog.show()

        /* Check every second if the permission was granted */
        fixedRateTimer("permissionCheck", false, 0, 1000) {
            if (hasPermissions()) {
                dialog.dismiss()
                this.cancel()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ancient = findViewById(R.id.ancient)
        slow = findViewById(R.id.slow)
        relaxed = findViewById(R.id.relaxed)
        loose = findViewById(R.id.loose)
        default = findViewById(R.id.default_speed)
        hasty = findViewById(R.id.hasty)
        snappy = findViewById(R.id.snappy)
        snappier = findViewById(R.id.snappier)
        fast = findViewById(R.id.fast)
        faster = findViewById(R.id.faster)
        instant  = findViewById(R.id.instant)

        speedButtonList = listOf(
            ancient,
            slow,
            relaxed,
            loose,
            default,
            hasty,
            snappy,
            snappier,
            fast,
            faster,
            instant
        )

        /* Setup scales for each button */
        speedButtonList.forEachIndexed { i, button ->
            /* Blend colors based on their position in the array */
            val buttonRatio = 1 - ((i + 1).toFloat() / speedButtonList.size)
            val colorBlend = blendColors(buttonColorMin, buttonColorMax, buttonRatio)
            button.setBackgroundColor(colorBlend)

            /* Setup click action */
            button.setOnClickListener {
                setAnimationSpeed(speedValueList[i])

                /* Start animation if stopped playing or does not exist */
                if (button.animation == null || button.animation.hasEnded()) {
                    val shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake)
                    shakeAnim.scaleCurrentDuration(speedValueList[i].toFloat())

                    button.startAnimation(shakeAnim)
                }
            }
        }

        /* Ensure we have the secure settings write permission */
        checkPermissions()
    }
}
