package com.lst.printerlib

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.lst.printerlib.printerface.DeviceFoundCallback
import com.lst.printerlib.printerface.IMyBinder
import com.lst.printerlib.printerface.ProcessData
import com.lst.printerlib.printerface.TaskCallback
import com.lst.printerlib.service.PrinterService
import com.lst.printerlib.utils.PrinterDev


class PrinterModel constructor(private val printerManager: PrinterManager) {

    private var mIMyBinder: IMyBinder? = null
    private val mServiceConnect = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mIMyBinder = service as IMyBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            startPrinterService()
        }
    }


    /**
     * @return return the bond devices
     */
    fun getBondDeviceList(deviceFoundCallback: DeviceFoundCallback): MutableList<String>? {
        mIMyBinder?.let {
            printerManager.getContext()?.let { ctx ->
                return it.onDiscovery(
                    ctx,
                    PrinterDev.PortType.Bluetooth,
                    deviceFoundCallback
                )
            }

        }
        return null
    }

    fun connectDeviceByMac(mac: String, taskCallback: TaskCallback) {
        mIMyBinder?.let {
            it.connectBtPort(mac, taskCallback)
            return
        }
        taskCallback.onFailed("mIMyBinder is not initialization,the lib was wrong")
    }

    fun disconnected(taskCallback: TaskCallback) {
        mIMyBinder?.let {
            it.disconnectCurrentPort(taskCallback)
            return
        }
        taskCallback.onFailed("mIMyBinder is not initialization,the lib was wrong")
    }

    fun writeData(taskCallback: TaskCallback, processData: ProcessData) {
        mIMyBinder?.let {
            it.writeSendData(taskCallback, processData)
            return
        }
        taskCallback.onFailed("mIMyBinder is not initialization,the lib was wrong")
    }

    fun writeData(byteArray: ByteArray, taskCallback: TaskCallback) {
        mIMyBinder?.let {
            it.write(byteArray, taskCallback)
            return
        }
        taskCallback.onFailed("mIMyBinder is not initialization,the lib was wrong")
    }


    fun startPrinterService(): PrinterModel {
        printerManager.getContext()?.let {
            val service = Intent(it, PrinterService::class.java)
            it.bindService(service, mServiceConnect, Service.BIND_AUTO_CREATE)
        }
        return this
    }


}