# 1、申请定位权限权限，搜索蓝牙

    这里用的是
    implementation 'pub.devrel:easypermissions:3.0.0'

# 2、扫描蓝牙

## 该方法返回的是已经绑定的设备列表，回调是扫描到的设备

    PrinterManager.getInstance().getBondDevicesList(object : DeviceFoundCallback {
        override fun deviceFoundCallback(device: DeviceInfo) {
            Log.e("MainActivity", device.deviceName + ":" + device.deviceAddress)
        }
    })
    