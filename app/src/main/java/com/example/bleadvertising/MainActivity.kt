package com.example.bleadvertising

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.UUID


class MainActivity : Activity() {
    private lateinit var mAdvertiseButton: Button
    private lateinit var stopAdvertiseButton: Button
    private lateinit var mBluetoothAdapter: BluetoothAdapter

    companion object {
        private const val TAG = "BLEApp"
        private const val REQUEST_ENABLE_BT = 1
        private const val Device_Name = "Abc"
        private const val Device_Id = "0x34Abc"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdvertiseButton = findViewById(R.id.adv)
        stopAdvertiseButton = findViewById(R.id.stopadv)

        mAdvertiseButton.setOnClickListener {
            if (savedInstanceState == null) {
                mBluetoothAdapter =
                    (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            }

            if (mBluetoothAdapter.isEnabled) {
                if (mBluetoothAdapter.isMultipleAdvertisementSupported) {
                    advertise(this)
                } else {
//                    showErrorText(R.string.bt_ads_not_supported)
                    Log.d(TAG, "not support")
                }
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@setOnClickListener
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        stopAdvertiseButton.setOnClickListener {
            stopAdv(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopAdv(context: Context) {
        val advertiser: BluetoothLeAdvertiser? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeAdvertiser
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        advertiser?.stopAdvertising(null)
    }

    private fun advertise(context: Context) {
        val advertiser: BluetoothLeAdvertiser? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeAdvertiser;
        val parameters: AdvertisingSetParameters?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parameters = AdvertisingSetParameters.Builder()
                .setLegacyMode(true)
                .setConnectable(false)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
                .build()
        } else {
            return
        }

        val manufacturerData = byteArrayOf(0x41, 0x4D, 0x4F, 0x4C)

        val testData = "abcdefghij"
        val testData1 = testData.toByteArray()

        val pUuid = ParcelUuid(UUID.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63"))

        val data = AdvertiseData.Builder()
            .addManufacturerData(1, testData1)
            .setIncludeDeviceName(true)
            .build()

        val callback: AdvertisingSetCallback?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            callback = object : AdvertisingSetCallback() {
                override fun onAdvertisingSetStarted(
                    advertisingSet: AdvertisingSet,
                    txPower: Int,
                    status: Int
                ) {
                    super.onAdvertisingSetStarted(advertisingSet, txPower, status)
                    Log.d(TAG, "onAdvertisingSetStarted(): txPower:$txPower , status: $status")

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    advertisingSet.setAdvertisingData(
                        AdvertiseData.Builder()
                            .addManufacturerData(67, testData1)
                            .setIncludeDeviceName(true)
                            .setIncludeTxPowerLevel(true)
                            .build()
                    )

                    val pUuid = pUuid
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE),
                            100
                        )
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    advertisingSet.setScanResponseData(
                        AdvertiseData.Builder().addServiceUuid(pUuid).build()
                    )
                    Log.d(TAG, "UUID$pUuid")
                }

                override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet) {
                    super.onAdvertisingSetStopped(advertisingSet)
                    Log.d(TAG, "onAdvertisingSetStopped():")
                }

                override fun onAdvertisingEnabled(
                    advertisingSet: AdvertisingSet,
                    enable: Boolean,
                    status: Int
                ) {
                    super.onAdvertisingEnabled(advertisingSet, enable, status)
                }

                override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int) {
                    super.onAdvertisingDataSet(advertisingSet, status)
                    Log.d(TAG, "onAdvertisingDataSet() :status:$status")
                    Log.d(TAG, "onAdvertisingDataSet() : advertisingSet:$advertisingSet")
                }

                override fun onScanResponseDataSet(advertisingSet: AdvertisingSet, status: Int) {
                    super.onScanResponseDataSet(advertisingSet, status)
                    Log.d(TAG, "onScanResponseDataSet(): status:$status")
                    Log.d(TAG, "onScanResponseDataSet() : advertisingSet:$advertisingSet")
                }

            }
        } else {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), 100)
                return
            }
            advertiser?.startAdvertisingSet(parameters, data, null, null, null, callback)
            Toast.makeText(this@MainActivity, "Data$data", Toast.LENGTH_LONG).show()
        }
    }

    private fun showErrorText(resId: Int) {
        // Implement your error handling logic here
    }
}
