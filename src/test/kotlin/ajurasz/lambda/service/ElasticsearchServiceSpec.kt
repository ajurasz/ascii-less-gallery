package ajurasz.lambda.service

import ajurasz.customGson
import ajurasz.model.GalleryItem
import ajurasz.service.ElasticsearchService
import ajurasz.toJson
import ajurasz.toObject
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.FreeSpec
import io.searchbox.client.JestClient
import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.transport.client.PreBuiltTransportClient
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties.*
import java.net.InetAddress
import java.util.concurrent.TimeUnit.MINUTES

@Suppress("UNCHECKED_CAST")
class ElasticsearchServiceSpec : FreeSpec() {

    companion object {
        val HTTP_PORT_VALUE = 9930
        val TCP_PORT_VALUE = 9931
        val INDEX_NAME_VALUE = "gallery"
        val CLUSTER_NAME_VALUE = "test-cluster"
        val embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("5.0.0")
                .withSetting(HTTP_PORT, HTTP_PORT_VALUE)
                .withSetting(TRANSPORT_TCP_PORT, TCP_PORT_VALUE)
                .withSetting(CLUSTER_NAME, CLUSTER_NAME_VALUE)
                .withStartTimeout(1, MINUTES)
                .withEsJavaOpts("-Xms128m -Xmx512m")
                .withIndex(INDEX_NAME_VALUE)
                .build()
                .start()

        val client = createClient()
        private fun createClient(): Client {
            val settings = Settings.builder().put(CLUSTER_NAME, CLUSTER_NAME_VALUE).build()
            return PreBuiltTransportClient(settings)
                    .addTransportAddress(InetSocketTransportAddress(InetAddress.getByName("localhost"), TCP_PORT_VALUE));
        }

        val jestClient = createJestClient()
        private fun createJestClient(): JestClient {
            val factory = JestClientFactory()
            factory.setHttpClientConfig(HttpClientConfig.Builder("http://localhost:$HTTP_PORT_VALUE").gson(customGson()).build())
            return factory.`object`
        }
    }

    val elasticsearchService = ElasticsearchService(jestClient)

    init {
        "Elasticsearch service" - {
            "Should add gallery item" {
                // given
                embeddedElastic.recreateIndices() // TODO: move to before
                val email = "foo@bar.com"
                val item = GalleryItem("id", "ascii", listOf("a", "b", "c"))

                // when
                val result = elasticsearchService.add(email, item)
                embeddedElastic.refreshIndices()

                // then
                result shouldBe true
                val query = client
                        .prepareSearch(INDEX_NAME_VALUE)
                        .setTypes(email)
                        .setQuery(QueryBuilders.matchAllQuery())
                        .execute().actionGet()
                query.hits.totalHits shouldEqual 1L
                val source = query.hits.hits[0].sourceAsString.toObject(Map::class.java)
                source["id"] shouldEqual item.id
                source["ascii"] shouldEqual item.ascii
                source["labels"] shouldEqual item.labels
            }

            "Should list gallery items" {
                // given
                embeddedElastic.recreateIndices() // TODO: move to before
                val email = "foo@bar.com"
                val item1 = GalleryItem("1", "ascii1", listOf("a1", "b1", "c1"))
                val item2 = GalleryItem("2", "ascii2", listOf("a2"))
                embeddedElastic.index(INDEX_NAME_VALUE, email, mapOf(item1.id to item1.toJson()) as Map<CharSequence, CharSequence>)
                embeddedElastic.index(INDEX_NAME_VALUE, email, mapOf(item2.id to item2.toJson()) as Map<CharSequence, CharSequence>)

                // when
                val result = elasticsearchService.list()

                // then
                result.size shouldBe 2
                val query1 = client
                        .prepareSearch(INDEX_NAME_VALUE)
                        .setTypes(email)
                        .setQuery(QueryBuilders.termQuery("id", item1.id))
                        .execute().actionGet()
                query1.hits.totalHits shouldBe 1L
                val source1 = query1.hits.hits[0].sourceAsString.toObject(Map::class.java)
                source1["id"] shouldEqual item1.id
                source1["ascii"] shouldEqual item1.ascii
                source1["labels"] shouldEqual item1.labels

                val query2 = client
                        .prepareSearch(INDEX_NAME_VALUE)
                        .setTypes(email)
                        .setQuery(QueryBuilders.termQuery("id", item2.id))
                        .execute().actionGet()
                query2.hits.totalHits shouldBe 1L
                val source2 = query2.hits.hits[0].sourceAsString.toObject(Map::class.java)
                source2["id"] shouldEqual item2.id
                source2["ascii"] shouldEqual item2.ascii
                source2["labels"] shouldEqual item2.labels

            }

            "Should remove gallery item" {
                // given
                embeddedElastic.recreateIndices() // TODO: move to before
                val email = "foo@bar.com"
                val item1 = GalleryItem("id", "ascii1", listOf("a1", "b1", "c1"))
                embeddedElastic.index(INDEX_NAME_VALUE, email, mapOf(item1.id to item1.toJson()) as Map<CharSequence, CharSequence>)

                // when
                val result = elasticsearchService.remove(item1.id, email)
                embeddedElastic.refreshIndices()

                // then
                result shouldBe true
                val query1 = client
                        .prepareSearch(INDEX_NAME_VALUE)
                        .setTypes(email)
                        .setQuery(QueryBuilders.termQuery("id", item1.id))
                        .execute().actionGet()
                query1.hits.totalHits shouldBe 0L
            }

            "Should not remove gallery item from other users" {
                // given
                embeddedElastic.recreateIndices() // TODO: move to before
                val email = "foo@bar.com"
                val item1 = GalleryItem("1", "ascii1", listOf("a1", "b1", "c1"))
                embeddedElastic.index(INDEX_NAME_VALUE, email, mapOf(item1.id to item1.toJson()) as Map<CharSequence, CharSequence>)

                // when
                val result = elasticsearchService.remove(item1.id, "other@other.com")

                // then
                result shouldBe false
                val query1 = client
                        .prepareSearch(INDEX_NAME_VALUE)
                        .setTypes(email)
                        .setQuery(QueryBuilders.termQuery("id", item1.id))
                        .execute().actionGet()
                query1.hits.totalHits shouldBe 1L
            }
        }
    }
}