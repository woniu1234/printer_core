package com.lst.printerlib.printerface

import com.lst.printerlib.DeviceInfo

/**
 *  扫描蓝牙发现设备callback
 */
interface DeviceFoundCallback {
    fun deviceFoundCallback(device: DeviceInfo)
}