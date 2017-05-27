package ajurasz.model

import io.searchbox.annotations.JestId
import java.util.*

data class GalleryItem(@JestId val id: String, val ascii: String, val labels: List<String>,
                       val createDate: Date = Date())