package com.andrew.studio.comap.activities

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.andrew.studio.comap.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*

class SplashActivity : AppCompatActivity() {

    private val TAG = "SPLASH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            (findViewById<LinearLayout>(R.id.parent_layout) as ViewGroup)
                    .layoutTransition
                    .enableTransitionType(LayoutTransition.CHANGING)
        }

        Dexter.withActivity(this).withPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (!report?.areAllPermissionsGranted()!!) {
                            AlertDialog.Builder(this@SplashActivity)
                                    .setTitle("Permissions required")
                                    .setMessage("All the permissions are required to do some magic!")
                                    .setIcon(R.drawable.ic_warning_amber_500_24dp)
                                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                        dialog?.dismiss()
                                        finish()
                                    }
                                    .create().show()
                        } else {
                            val timer = Timer()
                            timer.schedule(object : TimerTask() {
                                override fun run() {
                                    runOnUiThread {
                                        parent_layout.setPadding(0, 200, 0, 0)
                                        layout_mode_selection.visibility = View.VISIBLE

                                        btn_measuring.setOnClickListener {
                                            val view = LayoutInflater.from(this@SplashActivity).inflate(R.layout.dialog_input_password, null)

                                            val dialog = android.support.v7.app.AlertDialog.Builder(this@SplashActivity)
                                                    .setTitle("Enter secrete code")
                                                    .setMessage("Please enter your provided secrete code to continue")
                                                    .setView(view)
                                                    .create()
                                            dialog.show()
                                            val edt_input = view.findViewById<EditText>(R.id.edt_input)
                                            edt_input.hint = "Secrete code"

                                            val btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
                                            btn_cancel.setOnClickListener {
                                                dialog.dismiss()
                                            }
                                            val btn_ok = view.findViewById<Button>(R.id.btn_ok)
                                            btn_ok.text = "continue"
                                            btn_ok.setOnClickListener {
                                                if (edt_input.text.toString() == "123456") {
                                                    startActivity(Intent(this@SplashActivity, SessionSelect::class.java))
                                                    dialog.dismiss()
                                                } else {
                                                    Snackbar.make(parent_layout, "Secrete code incorrect! please try again.", Snackbar.LENGTH_LONG)
                                                            .setAction("OK") { }
                                                            .show()
                                                    dialog.dismiss()
                                                }
                                            }
                                        }

                                        btn_supervising.setOnClickListener {
                                            val intent = Intent(this@SplashActivity, SuperviseActivity::class.java)
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }, 1500)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }
                }).check()

    }
}
