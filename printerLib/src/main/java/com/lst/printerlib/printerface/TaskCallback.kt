package com.lst.printerlib.printerface

interface TaskCallback {
    fun onSucceed()

    fun onFailed(error: String)
}