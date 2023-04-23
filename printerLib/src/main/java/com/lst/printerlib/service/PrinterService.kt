package com.lst.printerlib.service

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.lst.printerlib.DeviceInfo
import com.lst.printerlib.printerface.DeviceFoundCallback
import com.lst.printerlib.printerface.IMyBinder
import com.lst.printerlib.printerface.ProcessData
import com.lst.printerlib.printerface.TaskCallback
import com.lst.printerlib.utils.PrintToastUtils
import com.lst.printerlib.utils.PrinterDev
import com.lst.printerlib.utils.ReturnMessage
import com.lst.printerlib.utils.RoundQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Arrays


class PrinterService : Service() {
    private val mMyBinder = MyBinder()
    private lateinit var mPrinterDev: PrinterDev
    private lateinit var mReturnMsg: ReturnMessage
    private var mIsConnected = false
    private var mQueue: RoundQueue<ByteArray>? = null
    private var mDeviceFoundCallback: DeviceFoundCallback? = null
    private fun getInstanceRoundQueue(): RoundQueue<ByteArray> {
        if (this.mQueue == null) {
            mQueue = RoundQueue(500)
        }
        return this.mQueue!!
    }

    override fun onCreate() {
        super.onCreate()
        this.mQueue = this.getInstanceRoundQueue()
    }


    private val mViewModelScope = CoroutineScope(Dispatchers.IO)
    override fun onBind(p0: Intent?): IBinder {
        return this.mMyBinder
    }


    inner class MyBinder : Binder(), IMyBinder {
        private var mBluetoothAdapter: BluetoothAdapter? = null
        private var mFond: MutableList<DeviceInfo> = arrayListOf()
        private var mBond: MutableList<DeviceInfo>? = null
        private var mPortType: PrinterDev.PortType? = null
        private val mReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                p1?.let {
                    if (it.action == "android.bluetooth.device.action.FOUND") {
                        val device =
                            it.getParcelableExtra<BluetoothDevice>("android.bluetooth.device.extra.DEVICE")
                                ?: return
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(
                                    this@PrinterService,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                if (!device.name.isNullOrEmpty()) {
                                    mFond.forEach { found ->
                                        if (found.deviceAddress == device.address) {
                                            return
                                        }
                                    }
                                    val deviceInfo = DeviceInfo()
                                    deviceInfo.deviceName = device.name
                                    deviceInfo.deviceAddress = device.address
                                    mFond.add(deviceInfo)
                                    mDeviceFoundCallback?.deviceFoundCallback(deviceInfo)
                                }
                            }
                        } else {
                            if (!device.name.isNullOrEmpty()) {
                                mFond.forEach { found ->
                                    if (found.deviceAddress == device.address) {
                                        return
                                    }
                                }
                                val deviceInfo = DeviceInfo()
                                deviceInfo.deviceName = device.name
                                deviceInfo.deviceAddress = device.address
                                mFond.add(deviceInfo)
                                mDeviceFoundCallback?.deviceFoundCallback(deviceInfo)
                            }
                        }
                    }
                }
            }
        }

        override fun connectBtPort(var1: String, var2: TaskCallback) {
            mViewModelScope.launch {
                mPrinterDev = PrinterDev(PrinterDev.PortType.Bluetooth, var1)
                mReturnMsg = mPrinterDev.open()
                mPortType = PrinterDev.PortType.Bluetooth
                mViewModelScope.launch(Dispatchers.Main) {
                    when (mReturnMsg.getErrorCode()) {
                        PrinterDev.ErrorCode.OpenPortSucceed -> {
                            mIsConnected = true
                            var2.onSucceed()
                        }

                        else -> {
                            var2.onFailed("Bluetooth connected Failed")
                        }
                    }
                }
            }
        }

        override fun disconnectCurrentPort(var1: TaskCallback) {
            mViewModelScope.launch {
                mReturnMsg = mPrinterDev.close()
                mViewModelScope.launch(Dispatchers.Main) {
                    when (mReturnMsg.getErrorCode()) {
                        PrinterDev.ErrorCode.ClosePortSucceed -> {
                            mIsConnected = false
                            if (mQueue != null) {
                                mQueue?.clear()
                            }
                            var1.onSucceed()
                        }

                        else -> {
                            var1.onFailed("Bluetooth disconnected failed")
                        }
                    }
                }
            }.start()
        }

        override fun clearBuffer() {
            mQueue?.clear()
        }

        override fun checkLinkedState(var1: TaskCallback) {
            mViewModelScope.launch {
                if (mPrinterDev.getPortInfo().isOpened) var1.onSucceed() else var1.onFailed("")
            }.start()
        }

        override fun onDiscovery(
            var1: Context,
            portType: PrinterDev.PortType,
            callback: DeviceFoundCallback
        ): MutableList<DeviceInfo>? {
            this.mFond = mutableListOf()
            this.mBond = mutableListOf()
            mDeviceFoundCallback = callback
            if (portType == PrinterDev.PortType.Bluetooth) {
                val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                this.mBluetoothAdapter = manager.adapter
                if (mBluetoothAdapter == null) {
                    PrintToastUtils.show("该设备没有蓝牙，不支持此功能", this@PrinterService)
                    return null
                }
                mBluetoothAdapter?.let {
                    if (it.isEnabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(
                                    this@PrinterService,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                if (it.isDiscovering) {
                                    it.cancelDiscovery()
                                }
                                it.startDiscovery()
                                val filter = IntentFilter("android.bluetooth.device.action.FOUND")
                                registerReceiver(mReceiver, filter)
                                val pairedDevice = it.bondedDevices
                                if (!pairedDevice.isNullOrEmpty()) {
                                    val iterator = pairedDevice.iterator()
                                    while (iterator.hasNext()) {
                                        val device = iterator.next()
                                        val deviceInfo = DeviceInfo()
                                        deviceInfo.deviceName = device.name
                                        deviceInfo.deviceAddress = device.address
                                        mBond?.add(deviceInfo)
                                    }
                                } else {
                                    PrintToastUtils.show(
                                        "该设备没有蓝牙，不支持此功能",
                                        this@PrinterService
                                    )
                                }
                            } else {
                                PrintToastUtils.show(
                                    "没有蓝牙连接权限，请退出重试",
                                    this@PrinterService
                                )
                            }
                        } else {
                            if (it.isDiscovering) {
                                it.cancelDiscovery()
                            }
                            it.startDiscovery()
                            val filter = IntentFilter("android.bluetooth.device.action.FOUND")
                            registerReceiver(mReceiver, filter)
                            val pairedDevice = it.bondedDevices
                            if (!pairedDevice.isNullOrEmpty()) {
                                val iterator = pairedDevice.iterator()
                                while (iterator.hasNext()) {
                                    val device = iterator.next()
                                    val deviceInfo = DeviceInfo()
                                    deviceInfo.deviceName = device.name
                                    deviceInfo.deviceAddress = device.address
                                    mBond?.add(deviceInfo)
                                }
                            } else {
                                PrintToastUtils.show(
                                    "该设备没有蓝牙，不支持此功能",
                                    this@PrinterService
                                )
                            }
                        }

                    } else {
                        PrintToastUtils.show("该设备没有蓝牙，不支持此功能", this@PrinterService)
                    }
                }
            }
            return this.mBond
        }

        override fun getBtAvailableDevice(): MutableList<DeviceInfo> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        this@PrinterService,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    this.mBluetoothAdapter?.cancelDiscovery()
                    return this.mFond
                }
            } else {
                this.mBluetoothAdapter?.cancelDiscovery()
                return this.mFond
            }
            return arrayListOf()
        }

        override fun write(var1: ByteArray?, var2: TaskCallback) {
            if (var1 != null) {
                mViewModelScope.launch {
                    mReturnMsg = mPrinterDev.write(var1)
                    mViewModelScope.launch(Dispatchers.Main) {
                        when (mReturnMsg.getErrorCode()) {
                            PrinterDev.ErrorCode.WriteDataSucceed -> {
                                mIsConnected = true
                                var2.onSucceed()
                            }

                            else -> {
                                mIsConnected = false
                                var2.onFailed("Write data failed ,please check the device connected")
                            }
                        }
                    }
                }
            }
        }

        override fun writeSendData(var1: TaskCallback, var2: ProcessData) {
            val list = var2.processDataBeforeSend()
            if (list == null) {
                var1.onFailed("Write data is null")
            } else {
                mViewModelScope.launch {
                    list.forEach {
                        mReturnMsg = mPrinterDev.write(it)
                    }
                    when (mReturnMsg.getErrorCode()) {
                        PrinterDev.ErrorCode.WriteDataSucceed -> {
                            mIsConnected = true
                            var1.onSucceed()
                        }

                        else -> {
                            mIsConnected = false
                            var1.onFailed("Bluetooth is no connected,please connect first")
                        }
                    }
                }.start()
            }
        }

        override fun acceptDataFromPrinter(var1: TaskCallback?, var2: Int) {
            val buffer = ByteArray(var2)
            mViewModelScope.launch {
                kotlin.runCatching {
                    mQueue = getInstanceRoundQueue()
                    mQueue?.clear()
                    mQueue?.addLast(buffer)
                    Log.i("frank", "acceptDataFromPrinter: " + Arrays.toString(mQueue!!.last))
                }.onSuccess {

                }.onFailure {

                }
            }
        }

        override fun readBuffer(): RoundQueue<ByteArray?>? {
            return null
        }

        override fun read(var1: TaskCallback) {
            mViewModelScope.launch {
                val msg = mPrinterDev.read()
                Log.d("frank", "read: $msg")
            }
        }

        override fun cancelDiscover() {
            mBluetoothAdapter?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this@PrinterService,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (it.isDiscovering) {
                            it.cancelDiscovery()
                        }
                    }
                } else {
                    if (it.isDiscovering) {
                        it.cancelDiscovery()
                    }
                }

            }
        }

    }
}