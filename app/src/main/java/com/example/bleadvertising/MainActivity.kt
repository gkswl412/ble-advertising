package com.example.bleadvertising

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.nio.ByteBuffer
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var mAdvertiseButton: Button
    private lateinit var stopAdvertiseButton: Button
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var advertisingSetCallback: AdvertisingSetCallback

    companion object {
        private const val TAG = "BLEApp"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_BLUETOOTH_PERMISSION = 2
        private const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3
        private const val STRING_UUID = "CDB7950D-73F1-4D4D-8E47-C090502DBD63"
    }

    // 권한 요청 프로세스
    // 1. 블루투스 권한 체크
    // 2. 블루투스 어댑터 on/off 체크
    // 3.

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
            }
            REQUEST_BLUETOOTH_CONNECT_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
            }
            REQUEST_ENABLE_BT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mBluetoothAdapter =
                        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

                    if (mBluetoothAdapter.isEnabled) {
                        if (mBluetoothAdapter.isMultipleAdvertisementSupported) {
                            advertise(this)
                        } else {
                            Log.d(TAG, "advertisement not support")
                        }
                    } else {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdvertiseButton = findViewById(R.id.adv)
        stopAdvertiseButton = findViewById(R.id.stopadv)

        mAdvertiseButton.setOnClickListener {
            Log.d(TAG, "start advertising")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_CONNECT_PERMISSION)
                } else {
                    mBluetoothAdapter =
                        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

                    if (mBluetoothAdapter.isEnabled) {
                        if (mBluetoothAdapter.isMultipleAdvertisementSupported) {
                            advertise(this)
                        } else {
                            Log.d(TAG, "advertisement not support")
                        }
                    } else {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    }
                }
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
                } else {
                    mBluetoothAdapter =
                        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

                    if (mBluetoothAdapter.isEnabled) {
                        if (mBluetoothAdapter.isMultipleAdvertisementSupported) {
                            advertise(this)
                        } else {
                            Log.d(TAG, "advertisement not support")
                        }
                    } else {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    }
                }
            }
        }

        stopAdvertiseButton.setOnClickListener {
            stopAdv(this, advertisingSetCallback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopAdv(context: Context, callback: AdvertisingSetCallback) {
        val advertiser: BluetoothLeAdvertiser? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeAdvertiser
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        advertiser?.stopAdvertisingSet(callback)
    }

    private fun advertise(context: Context) {
        val advertiser: BluetoothLeAdvertiser? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeAdvertiser;
        val parameters: AdvertisingSetParameters?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parameters = AdvertisingSetParameters.Builder()
                .setLegacyMode(true)
                .setConnectable(false)
                .setInterval(160)
                .setTxPowerLevel(1) // -127 ~ 1 (1이 가장 강한 강도)
                .build()
        } else {
            return
        }

        val pUuid = ParcelUuid(UUID.fromString(STRING_UUID))

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceData(pUuid, longToByteArray(System.currentTimeMillis())) // 현재 시간을 Advertising 데이터로 추가
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            advertisingSetCallback = object : AdvertisingSetCallback() {
                override fun onAdvertisingSetStarted(
                    advertisingSet: AdvertisingSet,
                    txPower: Int,
                    status: Int
                ) {
                    super.onAdvertisingSetStarted(advertisingSet, txPower, status)
                    Log.d(TAG, "onAdvertisingSetStarted(): txPower:$txPower , status: $status")
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(object : Runnable {
                        override fun run() {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_ADVERTISE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), 100)
                                return
                            }

                            advertisingSet.setAdvertisingData(
                                AdvertiseData.Builder()
                                    .addServiceData(pUuid, longToByteArray(System.currentTimeMillis())) // 현재 시간을 Advertising 데이터로 추가
                                    .build()
                            )

                            // 1초 후에 다시 실행합니다.
                            handler.postDelayed(this, 100)
                        }
                    }, 100)
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
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), 100)
                //return
            }
            advertiser?.startAdvertisingSet(parameters, data, null, null, null, advertisingSetCallback)
            Toast.makeText(this@MainActivity, "Data$data", Toast.LENGTH_LONG).show()
        }
    }


    private fun longToByteArray(value: Long): ByteArray {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(value)
        return buffer.array()
    }
}
