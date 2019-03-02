package scape.editor.gui.plugin

import java.net.URL
import java.net.URLClassLoader

class PluginClassLoader(val url: URL) : URLClassLoader(arrayOf(url))