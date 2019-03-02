package scape.editor.gui.util

import java.io.DataOutputStream
import java.io.IOException

@Throws(IOException::class)
fun DataOutputStream.write24Int(value: Int) {
    this.writeByte((value shr 16).toByte().toInt())
    this.writeByte((value shr 8).toByte().toInt())
    this.writeByte(value.toByte().toInt())
}