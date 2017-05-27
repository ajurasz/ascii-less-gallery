package ajurasz.service

import io.korhner.asciimg.image.AsciiImgCache
import io.korhner.asciimg.image.character_fit_strategy.StructuralSimilarityFitStrategy
import io.korhner.asciimg.image.converter.AsciiToStringConverter
import org.apache.log4j.Logger
import java.awt.Font
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class AsciiService {

    private val cache: AsciiImgCache = AsciiImgCache.create(Font("Courier", Font.BOLD, 6))
    private val converter: AsciiToStringConverter = AsciiToStringConverter(cache,
            StructuralSimilarityFitStrategy())

    fun imageToAscii(image: ByteArray): String {
        LOG.info("Convert image to ascii")
        val output = converter.convertImage(ImageIO.read(ByteArrayInputStream(image)))
        return output.toString()
    }

    companion object {
        val LOG: Logger = Logger.getLogger(AsciiService::class.java)
    }
}