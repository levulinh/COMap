package com.andrew.studio.comap.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.andrew.studio.comap.R
import com.andrew.studio.comap.config.Config
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import io.palaima.smoothbluetooth.Device
import io.palaima.smoothbluetooth.SmoothBluetooth
import kotlinx.android.synthetic.main.activity_measuring.*
import org.json.JSONObject
import android.content.Intent
import es.dmoral.toasty.Toasty
import java.util.concurrent.TimeUnit


class MeasuringActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var sessionCode: Int = 0
    private val config = Config
    private lateinit var queue: RequestQueue
    private lateinit var jsonObjectRequest: JsonObjectRequest
    private val TAG = "MEASURING_ACTIVITY"
    val ENABLE_BT_REQUEST = 1

    private lateinit var mSmoothBluetooth: SmoothBluetooth
    private lateinit var mSmoothListener: SmoothBluetooth.Listener
    private var mBuffer = mutableListOf<Int>()

    private lateinit var lastLocation: LatLng
    private var connectingSnackbar: Snackbar? = null
    private var discoveringSnackbar: Snackbar? = null
    private var lastReceived: Long = 0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measuring)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        sessionCode = intent.getIntExtra("code", 0)
        txt_session.text = sessionCode.toString()

        txt_session.setOnLongClickListener {
            // Copy session code text to clipboard
            val clipboardManager: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.primaryClip = ClipData.newPlainText("Session code", sessionCode.toString())
            Toast.makeText(this@MeasuringActivity, "Session code has been copied to your clipboard", Toast.LENGTH_SHORT).show()
            true
        }

        queue = Volley.newRequestQueue(this)

        val requestBody = JSONObject()
        requestBody
                .put("code", sessionCode)
                .put("isOnline", true)
        jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                config.SET_SESSION_STATUS,
                requestBody,
                Response.Listener {
                },
                Response.ErrorListener {
                }
        )
        queue.add(jsonObjectRequest)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    lastLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }

        mSmoothBluetooth = SmoothBluetooth(this)

        mSmoothListener = object : SmoothBluetooth.Listener {
            override fun onDevicesFound(deviceList: MutableList<Device>?, connectionCallback: SmoothBluetooth.ConnectionCallback?) {
                var index = -1
                for (i in 0 until deviceList!!.size) {
                    if (deviceList[i].name == "HC-05") {
                        index = i
                        break
                    }
                }
                if (index != -1) {
                    connectionCallback!!.connectTo(deviceList[index])
                } else {
                    AlertDialog.Builder(this@MeasuringActivity)
                            .setTitle("Cannot find CO-Buddy device")
                            .setMessage("Be sure that you've connected to \"HC-05\" bluetooth device")
                            .setIcon(R.drawable.ic_warning_amber_500_24dp)
                            .setNegativeButton("Back") { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }
                            .setPositiveButton("Try again") { dialog, _ ->
                                dialog.dismiss()
                                mSmoothBluetooth.doDiscovery()
                            }
                            .create().show()
                }
            }

            override fun onDiscoveryFinished() {
                Log.d(TAG, "Discovery Finished")
                if (discoveringSnackbar != null && discoveringSnackbar!!.isShownOrQueued) {
                    discoveringSnackbar!!.dismiss()
                }
            }

            override fun onConnecting(device: Device?) {
                Log.d(TAG, "Connecting")
                connectingSnackbar = Snackbar.make(parent_layout, "Connecting to the device, please wait...", Snackbar.LENGTH_INDEFINITE)
                connectingSnackbar!!.show()
            }

            override fun onDataReceived(data: Int) {
                mBuffer.add(data)
                var content = ""
                if (data == 10 && mBuffer[mBuffer.size - 2] == 13) {
                    for (i in 0 until mBuffer.size - 2) {
                        content = "$content${mBuffer[i].toChar()}"
                    }
                    mBuffer.clear()

                    if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastReceived) > 10) {
                        txt_data.text = "$content ppm"
                        txt_location.text = "${lastLocation.latitude}, ${lastLocation.longitude}"
                        val requestBody = JSONObject()
                        requestBody
                                .put("code", sessionCode)
                                .put("latitude", lastLocation.latitude)
                                .put("longitude", lastLocation.longitude)
                                .put("covalue", content.toInt())
                                // mock up the data
                                .put("temperature", 30)
                                .put("humidity", 70)
                        jsonObjectRequest = JsonObjectRequest(
                                Request.Method.POST,
                                config.UPDATE_LOCATION_URL,
                                requestBody,
                                Response.Listener {
                                    Toast.makeText(this@MeasuringActivity, "Data uploaded", Toast.LENGTH_SHORT).show()
                                },
                                Response.ErrorListener {
                                    Toast.makeText(this@MeasuringActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                                }
                        )
                        queue.add(jsonObjectRequest)
                    }

                    lastReceived = System.currentTimeMillis()
                }
            }

            override fun onBluetoothNotSupported() {
                android.app.AlertDialog.Builder(this@MeasuringActivity)
                        .setTitle("This device does not support bluetooth")
                        .setMessage("We are sorry that your device is not supported at this time")
                        .setIcon(R.drawable.ic_warning_amber_500_24dp)
                        .setNegativeButton("Back") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .create().show()
            }

            override fun onBluetoothNotEnabled() {
                val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetooth, ENABLE_BT_REQUEST)
            }

            override fun onConnected(device: Device?) {
                if (connectingSnackbar != null && connectingSnackbar!!.isShownOrQueued) {
                    connectingSnackbar!!.dismiss()
                }
                Snackbar.make(parent_layout, "Connected to CO-Buddy device", Snackbar.LENGTH_SHORT)
                        .setAction("OK") {}
                        .show()
            }

            override fun onDiscoveryStarted() {
                Log.d(TAG, "Discovery started")
                discoveringSnackbar = Snackbar.make(parent_layout, "Discovering bluetooth devices", Snackbar.LENGTH_INDEFINITE)
                discoveringSnackbar!!.show()
            }

            override fun onConnectionFailed(device: Device?) {
                if (device != null)
                    if (device.isPaired) mSmoothBluetooth.doDiscovery()
            }

            override fun onDisconnected() {
                Toasty.warning(this@MeasuringActivity, "Devices disconnected!", Toast.LENGTH_LONG, true).show()
            }

            override fun onNoDevicesFound() {
                AlertDialog.Builder(this@MeasuringActivity)
                        .setTitle("Cannot find CO-Buddy device")
                        .setMessage("Be sure that you've connected to \"HC-05\" bluetooth device")
                        .setIcon(R.drawable.ic_warning_amber_500_24dp)
                        .setNegativeButton("Back") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .setPositiveButton("Try again") { dialog, _ ->
                            dialog.dismiss()
                            mSmoothBluetooth.doDiscovery()
                        }
                        .create().show()
            }
        }

        mSmoothBluetooth.setListener(mSmoothListener)

        lastReceived = System.currentTimeMillis()

        mSmoothBluetooth.tryConnection()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()

        val requestBody = JSONObject()
        requestBody
                .put("code", sessionCode)
                .put("isOnline", false)
        jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                config.SET_SESSION_STATUS,
                requestBody,
                Response.Listener {
                },
                Response.ErrorListener {
                }
        )
        queue.add(jsonObjectRequest)

        try {
            mSmoothBluetooth.stop()
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ENABLE_BT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                mSmoothBluetooth.tryConnection()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */)
    }
}
