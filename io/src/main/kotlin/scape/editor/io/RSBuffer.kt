package scape.editor.fs.io

class RSBuffer private constructor(private var buffer: ByteArray) {

    var position: Int = 0

    fun readUByte(): Int {
        return buffer[position++].toInt() and 0xFF
    }

    fun readByte(): Byte {
        return buffer[position++]
    }

    fun readShort(): Int {
        position += java.lang.Short.BYTES
        return (buffer[position - 2].toInt() shl 8) + (buffer[position - 1].toInt() and 0xFF)
    }

    fun readUShort(): Int {
        position += java.lang.Short.BYTES
        return (buffer[position - 2].toInt() and 0xFF shl 8) + (buffer[position - 1].toInt() and 0xFF)
    }

    fun readInt(): Int {
        position += Integer.BYTES
        return (buffer[position - 4].toInt() and 0xFF shl 24) + (buffer[position - 3].toInt() and 0xFF shl 16) + (buffer[position - 2].toInt() and 0xFF shl 8) + (buffer[position - 1].toInt() and 0xFF)
    }

    fun writeByte(value: Int) {
        validateCapacity(java.lang.Byte.BYTES)
        buffer[position++] = value.toByte()
    }

    fun writeShort(value: Int) {
        validateCapacity(java.lang.Short.BYTES)
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
    }

    fun writeInt(value: Int) {
        validateCapacity(Integer.BYTES)
        buffer[position++] = (value shr 24).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
    }

    fun writeString10(s: String) {
        validateCapacity(s.length)
        System.arraycopy(s.toByteArray(), 0, buffer, position, s.length)
        position += s.length
        buffer[position++] = 10
    }

    private fun validateCapacity(length: Int) {
        if (position + length >= buffer.size) {
            val data = ByteArray(buffer.size * 2)
            System.arraycopy(buffer, 0, data, 0, buffer.size)
            this.buffer = data
        }
    }

    fun readUSmart(): Int {
        val value = buffer[position].toInt() and 0xff
        return if (value < 128) {
            readUByte() - 64
        } else {
            readUShort() - 49152
        }
    }

    fun readString10(): String {
        val startOffset = position
        while (buffer[position++].toInt() != 10);
        return String(buffer, startOffset, position - startOffset - 1)
    }

    fun capacity(): Int {
        return buffer.size
    }

    fun toArray(): ByteArray {
        val data = ByteArray(position)
        System.arraycopy(buffer, 0, data, 0, position)
        return data
    }

    companion object {

        private const val DEFAULT_CAPACITY = 5000

        @JvmStatic
        fun wrap(data: ByteArray): RSBuffer {
            return RSBuffer(data)
        }

        @JvmStatic
        fun init(capacity: Int = DEFAULT_CAPACITY): RSBuffer {
            return RSBuffer(ByteArray(capacity))
        }
    }

}
