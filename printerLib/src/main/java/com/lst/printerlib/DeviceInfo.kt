package com.lst.printerlib

class DeviceInfo {
    var deviceName: String? = null
    var deviceAddress: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceInfo

        if (deviceName != other.deviceName) return false
        if (deviceAddress != other.deviceAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceName?.hashCode() ?: 0
        result = 31 * result + (deviceAddress?.hashCode() ?: 0)
        return result
    }


}