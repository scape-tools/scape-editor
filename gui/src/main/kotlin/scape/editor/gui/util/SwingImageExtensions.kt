package scape.editor.gui.util

import java.awt.Image
import java.awt.image.BufferedImage

fun Image.imageToBufferedImage(): BufferedImage {
    val bufferedImage = BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB)
    val g2 = bufferedImage.createGraphics()
    g2.drawImage(this, 0, 0, null)
    g2.dispose()
    return bufferedImage
}