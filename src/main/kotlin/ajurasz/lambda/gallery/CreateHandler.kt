package ajurasz.lambda.gallery

import ajurasz.model.GalleryItem
import ajurasz.service.AsciiService
import ajurasz.service.ElasticsearchService
import ajurasz.service.RekognitionService
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.log4j.Logger
import java.util.*
import kotlin.collections.List as List

class CreateHandler(private val asciiService: AsciiService,
                    private val esService: ElasticsearchService,
                    private val rekognitionService: RekognitionService)
    : RequestHandler<CreateHandler.Request, String> {

    constructor() : this(AsciiService(), ElasticsearchService(), RekognitionService())

    override fun handleRequest(input: Request, context: Context): String {
        LOG.debug("input(\n$input\n)")

        if (input.base64Image == null || (input.base64Image as String).isBlank())
            return response(400, "Invalid identify request: no or empty 'base64Image' field supplied")

        val imageInBytes = base64ToBytes(input.base64Image!!)
        val labels = rekognitionService.imageLabels(imageInBytes)

        val galleryItem = createGalleryItem(asciiService, imageInBytes, labels)
        val result = esService.add(input.principalId!!, galleryItem)

        when(result) {
            true -> return galleryItem.id
            else -> return response(500, "Failed to save image")
        }
    }

    private fun response(statusCode: Int, message: String) = "[$statusCode] $message"

    private fun base64ToBytes(base64Image: String) = Base64.getDecoder().decode(base64Image)

    private fun createGalleryItem(asciiService: AsciiService, image: ByteArray, labels: List<String>): GalleryItem {
        LOG.info("Creating gallery item")
        return GalleryItem(UUID.randomUUID().toString(),
                asciiService.imageToAscii(image),
                labels)
    }

    data class Request(var base64Image: String? = null,
                       var typ: String? = null,
                       var principalId: String? = null)

    companion object {
        val LOG: Logger = Logger.getLogger(CreateHandler::class.java)
    }
}
