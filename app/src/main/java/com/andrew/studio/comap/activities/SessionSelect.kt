package com.andrew.studio.comap.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.andrew.studio.comap.R
import com.andrew.studio.comap.config.Config
import com.andrew.studio.comap.helper.DatabaseHelper
import com.andrew.studio.comap.helper.SessionAdapter
import com.andrew.studio.comap.models.Sessions
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_session_select.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.json.JSONObject


class SessionSelect : AppCompatActivity() {

    val database: DatabaseHelper get() = DatabaseHelper(applicationContext)
    private lateinit var viewAdapter: SessionAdapter
    private var sessions = listOf<Sessions>()
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_select)

        queue = Volley.newRequestQueue(this)

        btn_add_new.setOnClickListener {
            val intent = Intent(this, CodeGenerator::class.java)
            startActivity(intent)
        }

        btn_continue.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)

            val dialog = AlertDialog.Builder(this)
                    .setTitle("Continue a session")
                    .setMessage("Enter a session code you want to continue")
                    .setView(view)
                    .create()
            dialog.show()
            val edt_input = view.findViewById<EditText>(R.id.edt_input)
            edt_input.hint = "Session Code"

            val btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
            btn_cancel.setOnClickListener {
                dialog.dismiss()
            }
            val btn_ok = view.findViewById<Button>(R.id.btn_ok)
            btn_ok.setOnClickListener {
                edt_input.isEnabled
                val intent = Intent(this@SessionSelect, PreparingActivity::class.java)
                intent.putExtra("code", edt_input.text.toString().toInt())
                startActivity(intent)
                dialog.dismiss()
            }
        }

        sessions = database.use {
            select(Sessions.TABLE_NAME).exec { parseList(classParser<Sessions>()) }
        }

        if (sessions.isEmpty()) {
            txt_guide.visibility = View.VISIBLE
            rv_sessions.visibility = View.GONE
        } else {
            txt_guide.visibility = View.GONE
            rv_sessions.visibility = View.VISIBLE
        }

        viewAdapter = SessionAdapter(this, sessions)
        rv_sessions.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@SessionSelect, LinearLayoutManager.VERTICAL, false)
            adapter = viewAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        sessions = database.use {
            select(Sessions.TABLE_NAME).exec { parseList(classParser<Sessions>()) }
        }

        viewAdapter.updateData(sessions)

        if (sessions.isEmpty()) {
            txt_guide.visibility = View.VISIBLE
            rv_sessions.visibility = View.GONE
        } else {
            txt_guide.visibility = View.GONE
            rv_sessions.visibility = View.VISIBLE
        }
    }
}
