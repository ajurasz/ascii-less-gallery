package ajurasz.lambda.gallery

import ajurasz.service.AsciiService
import ajurasz.service.ElasticsearchService
import ajurasz.service.RekognitionService
import com.nhaarman.mockito_kotlin.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.FreeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class CreateHandlerSpec : FreeSpec() {

    val asciiServiceMock: AsciiService = mock()
    val esServiceMock: ElasticsearchService = mock()
    val rekognitionServiceMock: RekognitionService = mock()
    val createHandler = CreateHandler(
            asciiServiceMock,
            esServiceMock,
            rekognitionServiceMock)

    init {
        "Gallery POST CreateHandler" - {
            "Should return 400" {
                // when
                val result = createHandler.handleRequest(CreateHandler.Request(), mock())

                // then
                result shouldBe "[400] Invalid identify request: no or empty 'base64Image' field supplied"
            }

            "Should return 500" {
                // given
                val base64Image = "foo"

                // when
                whenever(asciiServiceMock.imageToAscii(any())).thenReturn("foo")
                whenever(esServiceMock.add(any(), any())).thenReturn(false)
                val result = createHandler.handleRequest(CreateHandler.Request(base64Image, null, "bar"), mock())

                // then
                result shouldBe "[500] Failed to save image"
            }

            "Should create gallery item" {
                // given
                val ascii = "ascii"
                val base64Image = "foo"
                val principalId = "bar"
                val labels = listOf("label1", "label2")

                // when
                whenever(asciiServiceMock.imageToAscii(any())).thenReturn(ascii)
                whenever(rekognitionServiceMock.imageLabels(any())).thenReturn(labels)

                val result = createHandler.handleRequest(CreateHandler.Request(base64Image, null, principalId),
                        mock())

                // then
                verify(esServiceMock).add(check {
                    assertEquals(principalId, it)
                }, check {
                    assertEquals(ascii, it.ascii)
                    assertEquals(labels, it.labels)
                    assertNotNull(it.id)
                })
                result shouldNotBe null
            }
        }
    }
}