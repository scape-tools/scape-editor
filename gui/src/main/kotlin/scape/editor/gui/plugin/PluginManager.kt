package scape.editor.gui.plugin

import com.google.common.eventbus.EventBus
import javafx.fxml.Initializable
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile


object PluginManager {

    val plugins = mutableSetOf<Any>()

    val classLoaderMap = mutableMapOf<Int, PluginClassLoader>()

    val jarNameMap = mutableMapOf<IPlugin, String>()

    val eventBus = EventBus()

    fun loadPlugins() {
        val paths = findPlugins()

        for (path in paths) {
            try {
                val loader = PluginClassLoader(path.toUri().toURL())

                val jar = JarFile(path.toFile())

                val entries = jar.entries()
                while(entries.hasMoreElements()) {
                    val next = entries.nextElement()

                    if (next.isDirectory || !next.name.endsWith(".class")) {
                        continue
                    }

                    var classpath = next.name

                    classpath = classpath.replace("/", ".")
                    classpath = classpath.replace(".class", "")

                    val clazz = loader.loadClass(classpath)

                    if (Modifier.isAbstract(clazz.modifiers) || !clazz.isAnnotationPresent(PluginDescriptor::class.java) || Initializable::class.java.isAssignableFrom(clazz) ) {
                        continue
                    }

                    val plugin = clazz.newInstance()

                    if (plugin is IPlugin) {
                        jarNameMap[plugin] = jar.name
                        classLoaderMap[plugin.hashCode()] = loader
                    }

                    plugins.add(plugin)
                    eventBus.register(plugin)
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        println("Loaded: ${plugins.size} plugins")

    }

    private fun findPlugins() : Set<Path> {
        val set = mutableSetOf<Path>()
        val root = Files.createDirectories(Paths.get(System.getProperty("user.home"), "scape-editor"))
        val pluginDir = Files.createDirectories(Paths.get(root.resolve("plugins").toUri()))

        Files.walk(pluginDir).filter { it -> it.fileName.toString().contains(".jar")}.distinct().forEach { set.add(it)}
        return set
    }

    fun post(event: Any) {
        eventBus.post(event)
    }

}