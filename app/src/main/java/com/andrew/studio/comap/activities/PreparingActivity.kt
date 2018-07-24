package com.andrew.studio.comap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.MessageQueue
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import com.andrew.studio.comap.R
import com.andrew.studio.comap.config.Config
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_session_select.*

class PreparingActivity : AppCompatActivity() {

    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preparing)

        val code = intent.getIntExtra("code", 0)
        queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(Request.Method.GET,
                "${Config.GET_SESSION_STATUS}/$code", null,
                Response.Listener { response ->
                    if (response.getBoolean("isOnline")) {
                        AlertDialog.Builder(this@PreparingActivity)
                                .setTitle("Session in used!")
                                .setMessage("This session is in used right now!")
                                .setPositiveButton("Back") { dialog, which ->
                                    finish()
                                    dialog.dismiss()
                                }.create().show()
                    } else {
                        val intent = Intent(this@PreparingActivity, MeasuringActivity::class.java)
                        intent.putExtra("code", code)
                        startActivity(intent)
                        finish()
                    }
                },
                Response.ErrorListener {
                    AlertDialog.Builder(this@PreparingActivity)
                            .setTitle("Session in used!")
                            .setMessage("This session is in used right now!")
                            .setPositiveButton("Back") { dialog, which ->
                                finish()
                                dialog.dismiss()
                            }
                })

        queue.add(request)
    }
}
