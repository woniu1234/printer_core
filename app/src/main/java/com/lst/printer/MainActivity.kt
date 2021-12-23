package com.lst.printer

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lst.printer.databinding.ActivityMainBinding
import com.lst.printerlib.DeviceInfo
import com.lst.printerlib.PrinterManager
import com.lst.printerlib.printerface.DeviceFoundCallback
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.tvPrinter.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        val perms = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(
                this,
                "扫描周围蓝牙设备需要获取位置权限",
                1001,
                *perms
            )
        } else {
            PrinterManager.getInstance().getBondDevicesList(object : DeviceFoundCallback {
                override fun deviceFoundCallback(device: DeviceInfo) {
                    Log.e("MainActivity", device.deviceName + ":" + device.deviceAddress)
                }
            })
        }

    }
}