package scape.editor.gui

import javafx.scene.image.Image
import org.apache.commons.lang.SystemUtils
import scape.editor.util.HashUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

object Settings {

    private val icons = HashMap<String, Image>()
    private val storeNames = HashMap<Int, String>()
    private val storeEntryNames = HashMap<Pair<Int, Int>, String>()
    private val hashes = HashMap<Int, String>()

    fun load() {
        loadIcons()
        loadSettings()
    }

    fun loadSettings() {
        val path: String

        if (!App.fs.isLoaded || !Files.exists(App.fs.root.resolve(".scape-settings.dat"))) {
            path = "/data/.scape-settings.dat"
        } else {
            path = App.fs.root.resolve(".scape-settings.dat").toString()
        }

        try {
            val stream : InputStream
            if (!App.fs.isLoaded || !Files.exists(App.fs.root.resolve(".scape-settings.dat"))) {
                stream = App::class.java.getResourceAsStream(path)
            } else {
                stream = FileInputStream(File(path))
            }

            DataInputStream(stream).use { dis ->
                val stores = dis.readUnsignedByte()
                for (i in 0 until stores) {
                    val id = dis.readUnsignedByte()
                    val name = dis.readUTF()

                    storeNames[id] = name
                }

                val storeEntries = dis.readInt()
                for (i in 0 until storeEntries) {
                    val store = dis.readUnsignedByte()
                    val file = dis.readInt()
                    val name = dis.readUTF()
                    storeEntryNames[Pair(store, file)] = name
                }
                val names = dis.readInt()
                for (i in 0 until names) {
                    putNameForHash(dis.readUTF())
                }
            }
        } catch (ex: Exception) {
            Files.deleteIfExists(Paths.get(path))
            ex.printStackTrace()
        }
    }

    fun save(path: Path = App.fs.root) {
        val bos = ByteArrayOutputStream()
        DataOutputStream(bos).use { dos ->
            dos.writeByte(storeNames.entries.size)
            for (entrySet in storeNames.entries) {
                dos.writeByte(entrySet.key)
                dos.writeUTF(entrySet.value)
            }

            dos.writeInt(storeEntryNames.entries.size)
            for (entrySet in storeEntryNames.entries) {
                val key = entrySet.key
                val storeId = key.first
                val fileId = key.second

                dos.writeByte(storeId)
                dos.writeInt(fileId)
                dos.writeUTF(entrySet.value)
            }

            dos.writeInt(hashes.size)
            for (name in hashes.values) {
                dos.writeUTF(name)
            }

        }

        val file = path.resolve(".scape-settings.dat").toFile()

        if (SystemUtils.IS_OS_WINDOWS) {
            Files.deleteIfExists(file.toPath()) // fix java bug not being able to override hidden files
            Runtime.getRuntime().exec("attrib +h ${file.path}")
        }

        FileOutputStream(file).use { fos ->
            fos.write(bos.toByteArray())
        }
    }

    fun getNameFromHash(hash: Int) : String? {
        return hashes[hash]
    }

    fun putNameForHash(name: String) {
        hashes[HashUtils.hashName(name)] = name
    }

    private fun loadIcons() {
        try {
            icons["file_store_32.png"] = Image(App::class.java.getResourceAsStream("/icons/file_store_32.png"))
            icons["dat_32.png"] = Image(App::class.java.getResourceAsStream("/icons/dat_32.png"))
            icons["file_32.png"] = Image(App::class.java.getResourceAsStream("/icons/file_32.png"))
            icons["gz_32.png"] = Image(App::class.java.getResourceAsStream("/icons/gz_32.png"))
            icons["idx_32.png"] = Image(App::class.java.getResourceAsStream("/icons/idx_32.png"))
        } catch (ex: IOException) {
            println("Failed to load icons.")
        }
    }

    fun putStoreName(storeId: Int, name: String) {
        storeNames[storeId] = name
    }

    fun getStoreReferenceName(storeId: Int) : String? {
        return storeNames[storeId]
    }

    fun getStoreEntryReferenceName(storeId: Int, fileId: Int) : String? {
        return storeEntryNames[Pair(storeId, fileId)]
    }

    fun putStoreEntryReferenceName(storeId: Int, fileId: Int, name: String) {
        storeEntryNames[Pair(storeId, fileId)] = name
    }

    fun getIcon(data: ByteArray) : Image? {
        if (isGzip(data)) {
            return Settings.getIcon("gz_32.png")
        } else if (data.isNotEmpty()) {
            return Settings.getIcon("dat_32.png")
        }

        return Settings.getIcon("file_32.png")
    }

    fun getIcon(name: String) : Image? {
        return icons[name]
    }

    fun isGzip(bytes: ByteArray): Boolean {
        if (bytes.size < 2) {
            return false
        }

        val head = bytes[0].toInt() and 0xff or (bytes[1].toInt() shl 8 and 0xff00)
        return GZIPInputStream.GZIP_MAGIC == head
    }

}