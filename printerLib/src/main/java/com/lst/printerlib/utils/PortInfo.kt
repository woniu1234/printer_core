package com.lst.printerlib.utils

import android.content.Context

data class PortInfo(
    var bluetoothId: String = "",
    var context: Context? = null,
    var parIsOk: Boolean = false,
    var isOpened: Boolean = false,
    var portType: PrinterDev.PortType? = null
)
