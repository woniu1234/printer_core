package com.lst.printerlib.printerface

/**
 * 组装打印数据
 */
interface ProcessData {
    fun processDataBeforeSend(): MutableList<ByteArray>?
}