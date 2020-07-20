package com.example.theftdetector

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule

const val DEVICE_NAME = "Maserati"
const val EMERGENCY_CONTACT = "2064994185"

class MainActivity : AppCompatActivity(), BLEControl.Callback {
    private var ble: BLEControl? = null
    private var messages: TextView? = null
    private var rssiAverage:Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter: BluetoothAdapter?
        adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            if (!adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
        messages = findViewById(R.id.bluetoothText)
        messages!!.movementMethod = ScrollingMovementMethod()
        ble = BLEControl(applicationContext, DEVICE_NAME)
        ActivityCompat.requestPermissions(this,
            arrayOf( Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE), 1)
    }

    override fun onRSSIread(uart:BLEControl,rssi:Int){
        rssiAverage = rssi.toDouble()
        writeLine("RSSI $rssiAverage")
    }

    fun clearText(v: View) {
        messages!!.text = ""
    }

    override fun onResume() {
        super.onResume()
        ble!!.registerCallback(this)
    }

    override fun onStop() {
        super.onStop()
        ble!!.unregisterCallback(this)
        ble!!.disconnect()
    }

    fun connect(v: View) {
        startScan()
    }

    private fun startScan() {
        writeLine("Scanning for devices ...")
        ble!!.connectFirstAvailable()
    }

    private fun writeLine(text: CharSequence) {
        runOnUiThread {
            messages!!.append(text)
            messages!!.append("\n")
        }
    }

    override fun onDeviceFound(device: BluetoothDevice) {
        writeLine("Found device : " + device.name)
        writeLine("Waiting for a connection ...")
    }

    override fun onDeviceInfoAvailable() {
        writeLine(ble!!.deviceInfo)
    }

    override fun onConnected(ble: BLEControl) {
        writeLine("Connected!")
        Timer().schedule(250) {
            ble.send("green")
        }
    }


    override fun onConnectFailed(ble: BLEControl) {
        writeLine("Error connecting to device!")
    }

    override fun onDisconnected(ble: BLEControl) {
        writeLine("Disconnected!")
    }


    override fun onReceive(ble: BLEControl, rx: BluetoothGattCharacteristic) {
        writeLine("Received value: " + rx.getStringValue(0))
        if (rx.getStringValue(0) == "d") {
            dialog()
        }
        if (rx.getStringValue(0) == "c") {
            call()
        }
    }

    private fun dialog() {
        runOnUiThread {
            val alertDialog: AlertDialog? = this?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setNegativeButton("cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                           cancel()
                        })
                }
                    // Set other dialog properties
                    .setMessage("Do you want to cancel the arlarm")
                    .setTitle("Arduino ALARM")
                // Create the AlertDialog
                builder.create()
            }
            alertDialog?.show()
        }
    }

    private fun call() {
        runOnUiThread {
            try {
                val uri: String = "tel:" + EMERGENCY_CONTACT
                val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // here to request the missing permissions, and then overriding
                    // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    // int[] grantResults)
                    // to handle the case where the user grants the permission.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.CALL_PHONE)) {
                    } else {
                        ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            1);
                    }

                }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {

            }
        }
    }

    fun cancel() {
        Timer().schedule(250) {
            ble!!.send("cancel")
        }
    }

    companion object {
        private val REQUEST_ENABLE_BT = 0
    }

}
