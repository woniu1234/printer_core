package com.lst.printerlib.utils

import java.nio.charset.Charset

fun charSetName(): Charset = Charset.forName("gbk")

fun strToBytes(str: String): ByteArray? {

    val data: ByteArray
    try {
        val b = str.toByteArray(charset = Charsets.UTF_8)
        data = String(b).toByteArray(Charset.forName("gbk"))
    } catch (e: Exception) {
        return null
    }
    return data
}


fun strToBytes(str: String, charset: String): ByteArray? {
    val data: ByteArray?
    try {
        val b = str.toByteArray(charset = Charsets.UTF_8)
        data = String(b).toByteArray(Charset.forName("gbk"))
    } catch (e: Exception) {
        return null
    }

    return data
}

