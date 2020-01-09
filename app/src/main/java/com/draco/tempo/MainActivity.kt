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

    private lateinit var help: Button

    private lateinit var slowOpenClose: Button
    private lateinit var slowDialogs: Button
    private lateinit var slowEverything: Button
    private lateinit var relaxedOpenClose: Button
    private lateinit var relaxedDialogs: Button
    private lateinit var relaxedEverything: Button
    private lateinit var defaultEverything: Button
    private lateinit var snappyOpenClose: Button
    private lateinit var snappyDialogs: Button
    private lateinit var snappyEverything: Button
    private lateinit var instantOpenClose: Button
    private lateinit var instantDialogs: Button
    private lateinit var instantEverything: Button

    /* Window, Transition, Animator */
    /* Pop-ups, App open, App switcher & app close */
    private val speedValueList = listOf(
        listOf(1.0, 1.5, 1.5),    /* Slow Open & Close */
        listOf(1.5, 1.0, 1.0),    /* Slow Dialogs */
        listOf(1.5, 1.5, 1.5),    /* Slow Everything */

        listOf(1.0, 1.2, 1.2),    /* Relaxed Open & Close */
        listOf(1.2, 1.0, 1.0),    /* Relaxed Dialogs */
        listOf(1.2, 1.2, 1.2),    /* Relaxed Everything */

        listOf(1.0, 1.0, 1.0),    /* Default Everything */

        listOf(1.0, 0.7, 0.7),    /* Snappy Open & Close */
        listOf(0.5, 1.0, 1.0),    /* Snappy Dialogs */
        listOf(0.5, 0.7, 0.7),    /* Snappy Everything */

        listOf(1.0, 0.0, 0.0),    /* Instant Open & Close */
        listOf(0.0, 1.0, 1.0),    /* Instant Dialogs */
        listOf(0.0, 0.0, 0.0)     /* Instant Everything */
    )

    /* Speed that the button animation will scale by */
    private val animSpeedValueList = listOf(
        1.5,    /* Slow Open & Close */
        1.5,    /* Slow Dialogs */
        1.5,    /* Slow Everything */

        1.2,    /* Relaxed Open & Close */
        1.2,    /* Relaxed Dialogs */
        1.2,    /* Relaxed Everything */

        1.0,    /* Default Everything */

        0.7,    /* Snappy Open & Close */
        0.7,    /* Snappy Dialogs */
        0.7,    /* Snappy Everything */

        0.0,    /* Instant Open & Close */
        0.0,    /* Instant Dialogs */
        0.0     /* Instant Everything */
    )

    private lateinit var speedButtonList: List<Button>

    private val buttonColorMin = Color.parseColor("#039be5")
    private val buttonColorMax = Color.parseColor("#f44336")

    /* Merge two colors by a ratio [0,1] */
    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRation = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRation
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRation
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRation
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    private fun setAnimationSpeed(scales: List<Double>) {
        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            scales[0].toFloat()
        )

        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            scales[1].toFloat()
        )

        Settings.Global.putFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            scales[2].toFloat()
        )
    }

    /* Setup colors and actions */
    private fun setupButtons() {
        help.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage(getString(R.string.help_message))
                .setPositiveButton("Dismiss", null)
                .show()
        }

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
                    shakeAnim.scaleCurrentDuration(animSpeedValueList[i].toFloat())

                    button.startAnimation(shakeAnim)
                }
            }
        }
    }

    private fun initializeUI() {
        help = findViewById(R.id.help)

        slowOpenClose = findViewById(R.id.slow_open_close)
        slowDialogs = findViewById(R.id.slow_dialogs)
        slowEverything = findViewById(R.id.slow_everything)
        relaxedOpenClose = findViewById(R.id.relaxed_open_close)
        relaxedDialogs = findViewById(R.id.relaxed_dialogs)
        relaxedEverything = findViewById(R.id.relaxed_everything)
        defaultEverything = findViewById(R.id.default_everything)
        snappyOpenClose = findViewById(R.id.snappy_open_close)
        snappyDialogs = findViewById(R.id.snappy_dialogs)
        snappyEverything = findViewById(R.id.snappy_everything)
        instantOpenClose = findViewById(R.id.instant_open_close)
        instantDialogs = findViewById(R.id.instant_dialogs)
        instantEverything = findViewById(R.id.instant_everything)

        speedButtonList = listOf(
            slowOpenClose,
            slowDialogs,
            slowEverything,
            relaxedOpenClose,
            relaxedDialogs,
            relaxedEverything,
            defaultEverything,
            snappyOpenClose,
            snappyDialogs,
            snappyEverything,
            instantOpenClose,
            instantDialogs,
            instantEverything
        )

        setupButtons()
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

        initializeUI()
        checkPermissions()
    }
}
