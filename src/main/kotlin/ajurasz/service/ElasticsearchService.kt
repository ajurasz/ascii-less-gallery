package ajurasz.service

import ajurasz.customGson
import ajurasz.model.GalleryItem
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import io.searchbox.client.JestClient
import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.Delete
import io.searchbox.core.Index
import io.searchbox.core.Search
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.log4j.Logger
import vc.inreach.aws.request.AWSSigner
import vc.inreach.aws.request.AWSSigningRequestInterceptor
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class ElasticsearchService {
    private val client: JestClient

    constructor() {
        this.client = createJestClient()
    }

    constructor(client: JestClient) {
        this.client = client
    }

    fun add(userEmail: String, galleryItem: GalleryItem): Boolean {
        val index = Index.Builder(galleryItem).index(INDEX_NAME).type(userEmail).build()
        val result = client.execute(index)

        return result.isSucceeded
    }

    fun list(from: Int = 0, size: Int = 10): List<GalleryItem> {
        val query = """{"from": $from, "size": $size, "query": { "match_all": {} }}"""
        val search = Search.Builder(query).addIndex(INDEX_NAME).build()
        val result = client.execute(search)

        return result?.getHits(GalleryItem::class.java)?.map { it.source } ?: emptyList()
    }

    fun remove(key: String, emial: String): Boolean {
        val delete = Delete.Builder(key).index(INDEX_NAME).type(emial).build()
        return client.execute(delete).jsonMap["found"] as Boolean? ?: false
    }

    private fun createJestClient(): JestClient {

        val awsSigner = AWSSigner(
                EnvironmentVariableCredentialsProvider(),
                Regions.US_EAST_1.getName(),
                "es",
                { LocalDateTime.now(ZoneOffset.UTC) })

        val requestInterceptor = AWSSigningRequestInterceptor(awsSigner)

        val factory = object : JestClientFactory() {
            override fun configureHttpClient(builder: HttpClientBuilder): HttpClientBuilder {
                builder.addInterceptorLast(requestInterceptor)
                return builder
            }

            override fun configureHttpClient(builder: HttpAsyncClientBuilder): HttpAsyncClientBuilder {
                builder.addInterceptorLast(requestInterceptor)
                return builder
            }
        }

        factory.setHttpClientConfig(HttpClientConfig.Builder(SERVICE_URL).gson(customGson()).build())

        return factory.`object`
    }

    companion object {
        val SERVICE_URL = System.getenv("ES_URL")
        val INDEX_NAME = "gallery"
        val LOG: Logger = Logger.getLogger(ElasticsearchService::class.java)
    }
}