package ajurasz.service

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder
import com.amazonaws.services.rekognition.model.DetectLabelsRequest
import com.amazonaws.services.rekognition.model.Image
import org.apache.log4j.Logger
import java.nio.ByteBuffer

class RekognitionService(private val amazonRekognition: AmazonRekognition) {

    constructor() : this(AmazonRekognitionClientBuilder
            .standard()
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .withRegion(Regions.US_EAST_1)
            .build())

    fun imageLabels(image: ByteArray): List<String> {
        LOG.info("Recognize image labels")
        val response = amazonRekognition.detectLabels(DetectLabelsRequest()
                .withMaxLabels(10)
                .withMinConfidence(60f)
                .withImage(Image().withBytes(ByteBuffer.wrap(image))))

        return response?.labels?.map { it.name }.orEmpty()
    }

    companion object {
        val LOG: Logger = Logger.getLogger(RekognitionService::class.java)
    }
}