package org.chef.mockdriver.util

import java.awt.image.BufferedImage
import java.util.Base64
import javax.imageio.ImageIO

/**
 * Created by Chef.Xie
 */
object ImageUtil {
    fun loadResourceImage(resourceName: String): BufferedImage {
        val inStream = javaClass.getResourceAsStream(resourceName)
        return ImageIO.read(inStream)
    }

    fun toBase64(resourceName: String): String {
        val inStream = javaClass.getResourceAsStream(resourceName)
        return Base64.getEncoder().encodeToString(inStream.readBytes())
    }
}