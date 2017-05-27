package ajurasz.lambda.service

import ajurasz.service.AsciiService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class AsciiServiceSpec : FreeSpec() {
    val asciiService = AsciiService()
    init {
        "Ascii Service" - {
            "Should convert image to ascii" {
                // given
                val image = javaClass.getResource("/cat.png").readBytes()

                // when
                val result = asciiService.imageToAscii(image)

                // then
                trimEnd(result) shouldBe javaClass.getResource("/cat.txt").readText()
            }
        }
    }

    private fun trimEnd(string: String) = string.split("\n").map { it.trimEnd() }.joinToString("\n")
}