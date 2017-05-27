package ajurasz.lambda.service

import ajurasz.service.RekognitionService
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.model.DetectLabelsResult
import com.amazonaws.services.rekognition.model.Label
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.FreeSpec

class RekognitionServiceSpec : FreeSpec() {
    val amazonRekognitionMock: AmazonRekognition = mock()
    val rekognitionService = RekognitionService(amazonRekognitionMock)
    init {
        "Rekognition service" - {
            "Should return image labels" {
                // given
                val labels = DetectLabelsResult()
                labels.setLabels(mutableListOf(
                        Label().withName("foo"),
                        Label().withName("bar")
                ))

                // when
                whenever(amazonRekognitionMock.detectLabels(any())).thenReturn(labels)
                val result = rekognitionService.imageLabels(kotlin.ByteArray(1))

                // then
                result.size shouldBe 2
                result[0] shouldEqual "foo"
                result[1] shouldEqual "bar"
            }
        }
    }
}