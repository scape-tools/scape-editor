package scape.editor.gui.util

import java.awt.Color
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.FilteredImageSource
import java.awt.image.RGBImageFilter

fun BufferedImage.setColorTransparent(color: Color): BufferedImage {
    val filter = object : RGBImageFilter() {

        var markerRGB = color.rgb or -0x1000000

        override fun filterRGB(x: Int, y: Int, rgb: Int): Int {
            return if (rgb or -0x1000000 == markerRGB) {
                0x00FFFFFF and rgb
            } else {
                rgb
            }
        }
    }

    val ip = FilteredImageSource(source, filter)
    return Toolkit.getDefaultToolkit().createImage(ip).imageToBufferedImage()
}

fun BufferedImage.toType(type: Int): BufferedImage {
    val image = BufferedImage(this.width, this.height, type)
    image.graphics.drawImage(this, 0, 0, null)
    return image
}