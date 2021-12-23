package com.lst.printerlib.printerface

import android.content.Context
import com.lst.printerlib.utils.PrinterDev
import com.lst.printerlib.utils.RoundQueue

interface IMyBinder {
    fun connectBtPort(var1: String, var2: TaskCallback)

    fun disconnectCurrentPort(var1: TaskCallback)

    fun clearBuffer()

    fun checkLinkedState(var1: TaskCallback)

    fun onDiscovery(
        var1: Context,
        portType: PrinterDev.PortType,
        callback: DeviceFoundCallback
    ): MutableList<String>?

    fun getBtAvailableDevice(): MutableList<String>

    fun write(var1: ByteArray?, var2: TaskCallback)

    fun writeSendData(var1: TaskCallback, var2: ProcessData)

    fun acceptDataFromPrinter(var1: TaskCallback?, var2: Int)

    fun readBuffer(): RoundQueue<ByteArray?>?

    fun read(var1: TaskCallback)
}