package com.andrew.studio.comap.helper

import android.content.Context
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.andrew.studio.comap.R
import com.andrew.studio.comap.activities.MeasuringActivity
import com.andrew.studio.comap.activities.PreparingActivity
import com.andrew.studio.comap.config.Config
import com.andrew.studio.comap.models.Sessions
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_session_select.*
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(private val context: Context, private var sessions: List<Sessions>) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return ViewHolder(v)
    }

    fun updateData(sessions: List<Sessions>) {
        this.sessions = sessions
        notifyDataSetChanged()
    }

    override fun getItemCount() = sessions.size

    override fun onBindViewHolder(holder: SessionAdapter.ViewHolder, position: Int) {
        val session = sessions[position]
        val txt_session_code = holder.itemView.findViewById<TextView>(R.id.txt_session_code)
        val txt_date = holder.itemView.findViewById<TextView>(R.id.txt_date)

        txt_session_code.text = "${session.code}"
        txt_date.text = SimpleDateFormat("hh:mm dd MMM yyyy").format(Date(session.createdAt))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PreparingActivity::class.java)
            intent.putExtra("code", session.code)
            startActivity(context, intent, null)
        }
    }

}