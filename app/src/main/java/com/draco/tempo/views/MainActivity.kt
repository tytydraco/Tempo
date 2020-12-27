package com.draco.tempo.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.draco.tempo.R
import com.draco.tempo.viewmodels.MainActivityViewModel
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var buttonContainer: LinearLayout

    private fun checkPermissions() {
        if (viewModel.hasPermissions())
            return

        val adbCommand = "pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.missingPermissions))
            .setMessage(getString(R.string.adb_tutorial) + "adb shell $adbCommand")
            .setPositiveButton(getString(R.string.buttonCheckAgain), null)
            .setNeutralButton(getString(R.string.buttonSetupADB), null)
            .setNegativeButton(getString(R.string.buttonUseRoot), null)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            /* We don't dismiss on Check Again unless we actually have the permission */
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (viewModel.hasPermissions())
                    dialog.dismiss()
            }

            /* Open tutorial but do not dismiss until user presses Check Again */
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                val uri = Uri.parse("https://www.xda-developers.com/install-adb-windows-macos-linux/")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }

            /* Try using root permissions */
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                try {
                    ProcessBuilder("su", "-c", adbCommand).start()
                    if (viewModel.hasPermissions())
                        dialog.dismiss()
                } catch (_: Exception) {}
            }
        }

        dialog.show()
    }

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
                viewModel.setAnimationSpeed(speed)
                if (!animation.hasStarted() || animation.hasEnded())
                    it.startAnimation(animation)
            }
            button.text = viewModel.formatSpeedText(rawSpeed)
            button.setBackgroundColor(viewModel.blendColors(speed))
            buttonContainer.addView(button)
        }

        /* Ensure we have the secure settings write permission */
        checkPermissions()
    }
}
