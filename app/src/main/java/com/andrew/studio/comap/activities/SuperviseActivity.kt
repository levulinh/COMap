package com.andrew.studio.comap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.andrew.studio.comap.R
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_supervise.*

class SuperviseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervise)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        btn_start.setOnClickListener {
            if (txt_session_code.text.isNotEmpty()) {
                val intent = Intent(this@SuperviseActivity, MapsActivity::class.java)
                intent.putExtra("code", txt_session_code.text.toString().toInt())
                startActivity(intent)
            } else {
                Toast.makeText(this@SuperviseActivity, "Please enter the session your want to see", Toast.LENGTH_LONG).show()
            }
        }

        btn_admin.setOnClickListener{
            val view = LayoutInflater.from(this@SuperviseActivity).inflate(R.layout.dialog_input_password, null)

            val dialog = android.support.v7.app.AlertDialog.Builder(this@SuperviseActivity)
                    .setTitle("Enter admin key")
                    .setMessage("Please enter your provided secrete code to continue")
                    .setView(view)
                    .create()
            dialog.show()
            val edt_input = view.findViewById<EditText>(R.id.edt_input)
            edt_input.hint = "Admin key"

            val btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
            btn_cancel.setOnClickListener {
                dialog.dismiss()
            }
            val btn_ok = view.findViewById<Button>(R.id.btn_ok)
            btn_ok.text = "continue"
            btn_ok.setOnClickListener {
                if (edt_input.text.toString() == "123456") {
                    val intentToMap = Intent(this@SuperviseActivity, MapsActivity::class.java)
                    intent.putExtra("mode", true)
                    startActivity(intentToMap)
                    dialog.dismiss()
                } else {
                    Snackbar.make(parent_layout, "Admin key incorrect! please try again.", Snackbar.LENGTH_LONG)
                            .setAction("OK") { }
                            .show()
                    dialog.dismiss()
                }
            }
        }
    }
}
