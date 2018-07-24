package com.andrew.studio.comap.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.andrew.studio.comap.R
import com.andrew.studio.comap.config.Config
import com.andrew.studio.comap.helper.DatabaseHelper
import com.andrew.studio.comap.models.Sessions
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_code_generator.*
import org.jetbrains.anko.db.insert
import java.text.DecimalFormat
import java.util.*

class CodeGenerator : AppCompatActivity() {

    private val config = Config
    private lateinit var queue: RequestQueue
    val database: DatabaseHelper get() = DatabaseHelper(applicationContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_generator)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)

        queue = Volley.newRequestQueue(this@CodeGenerator)

        addEvents()

    }

    private fun addEvents() {
        val jsonRequest = JsonObjectRequest(Request.Method.GET, config.NEW_SESSION_URL,
                null,
                Response.Listener { response ->
                    if (response.has("session-code")) {
                        runOnUiThread {
                            progress_loading.visibility = View.INVISIBLE
                            val df = DecimalFormat("#####")
                            txt_session_code?.visibility = View.VISIBLE
                            txt_session_code?.text = df.format(response.getInt("session-code"))
                            btn_start?.isEnabled = true
                            TransitionManager
                                    .beginDelayedTransition(findViewById(R.id.content_container))
                            txt_ready.visibility = View.VISIBLE

                            // Copy session code text to clipboard
                            val clipboardManager: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboardManager.primaryClip = ClipData.newPlainText("Session code", txt_session_code.text)
                            Toast.makeText(this@CodeGenerator, "Session code has been copied to your clipboard", Toast.LENGTH_SHORT).show()

                            btn_start.setOnClickListener {
                                val sessionCode = response.getInt("session-code")
                                database.use {
                                    insert(Sessions.TABLE_NAME,
                                            Sessions.COLUMN_CODE to sessionCode,
                                            Sessions.COLUMN_CREATED_AT to Calendar.getInstance().timeInMillis
                                    )
                                }

                                val intent = Intent(this@CodeGenerator, MeasuringActivity::class.java)
                                intent.putExtra("code", sessionCode)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                },
                Response.ErrorListener { error ->
                    Log.e("VOLLEY", error.message.toString())
                    runOnUiThread {
                        progress_loading.visibility = View.INVISIBLE
                        Toast.makeText(
                                this@CodeGenerator,
                                "Something happened, try again later!",
                                Toast.LENGTH_LONG)
                                .show()
                    }

                })
        queue.add(jsonRequest)
    }
}
