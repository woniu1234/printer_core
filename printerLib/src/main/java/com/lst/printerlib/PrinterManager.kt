package com.lst.printerlib

import android.content.Context
import com.lst.printerlib.printerface.DeviceFoundCallback
import com.lst.printerlib.printerface.ProcessData
import com.lst.printerlib.printerface.TaskCallback
import java.lang.ref.WeakReference

class PrinterManager private constructor() {
    private var context: WeakReference<Context>? = null
    private lateinit var printerModel: PrinterModel

    companion object {
        private var singleInstance: PrinterManager? = null
            get() {
                if (null == field) {
                    field = PrinterManager()
                }

                return field
            }

        @Synchronized
        fun getInstance() = singleInstance!!

    }

    fun initPrinter(context: Context) {
        this.context = WeakReference(context)
        printerModel = PrinterModel(this)
        printerModel.startPrinterService()
    }


    fun getContext(): Context? {
        return this.context?.get()
    }


    fun getBondDevicesList(foundCallback: DeviceFoundCallback): MutableList<DeviceInfo>? {
        return printerModel.getBondDeviceList(foundCallback)
    }

    fun cancelDiscover() {
        printerModel.cancelDiscover()
    }

    /**
     * @param[mac] the printer mac
     * @param[taskCallback] the connect state by the taskCallback call
     */
    fun connectDeviceByMac(mac: String, taskCallback: TaskCallback) {
        printerModel.connectDeviceByMac(mac, taskCallback)
    }

    /**
     * @param[taskCallback] the disconnect state by taskCallback call
     */
    fun disconnected(taskCallback: TaskCallback) {
        printerModel.disconnected(taskCallback)
    }


    /**
     * @param[taskCallback] write state by the taskCallback call
     * @param[processData] the send data
     */
    fun writeData(taskCallback: TaskCallback, processData: ProcessData) {
        printerModel.writeData(taskCallback, processData)
    }

    /**
     * @param[byteArray] the send data
     * @param[taskCallback] write state by the taskCallback call
     */
    fun writeData(byteArray: ByteArray, taskCallback: TaskCallback) {
        printerModel.writeData(byteArray, taskCallback)
    }


}