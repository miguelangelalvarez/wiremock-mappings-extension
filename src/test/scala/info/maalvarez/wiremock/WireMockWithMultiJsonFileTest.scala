package info.maalvarez.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.softwaremill.sttp._
import info.maalvarez.wiremock.mappingloader.MultiJsonFileMappingSource
import org.specs2.Specification
import org.specs2.specification.BeforeAfterAll
import org.specs2.specification.core.SpecStructure

class WireMockWithMultiJsonFileTest  extends Specification with BeforeAfterAll {
  val wireMockConfig: WireMockConfiguration =
    new WireMockConfiguration()
      .port(8889)
      .mappingSource(MultiJsonFileMappingSource("multi-request-response.json"))
  val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig)

  implicit val backend = HttpURLConnectionBackend()

  override def is: SpecStructure =
    s2"""The request should
        | return OK for KathMandu                       $shouldReturnOkStatusKathmandu
        | return the content type for KathMandu         $shouldReturnContentTypeKathmandu
        | return a non-empty body for KathMandu         $shouldReturnBodyKathmandu
        | return OK for Merida                          $shouldReturnOkStatusMerida
        | return the content type for Merida            $shouldReturnContentTypeMerida
        | return a non-empty body for Merida            $shouldReturnBodyMerida
        | return OK for Lhasa                           $shouldReturnOkStatusLhasa
        | return the content type for Lhasa             $shouldReturnContentTypeLhasa
        | return a non-empty body for Lhasa             $shouldReturnBodyLhasa
    """.stripMargin

  override def beforeAll: Unit = wireMockServer.start()

  override def afterAll(): Unit = wireMockServer.stop()

  def shouldReturnOkStatusKathmandu = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Kathmandu,np&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.is200 == true
  }

  def shouldReturnContentTypeKathmandu = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Kathmandu,np&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.contentType must beSome("application/json")
  }

  def shouldReturnBodyKathmandu = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Kathmandu,np&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.unsafeBody !== ""
  }

  def shouldReturnOkStatusMerida = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Merida,ve&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.is200 == true
  }

  def shouldReturnContentTypeMerida = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Merida,ve&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.contentType must beSome("application/json")
  }

  def shouldReturnBodyMerida = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Merida,ve&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.unsafeBody !== ""
  }

  def shouldReturnOkStatusLhasa = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Lhasa,cn&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.is200 == true
  }

  def shouldReturnContentTypeLhasa = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Lhasa,cn&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.contentType must beSome("application/json")
  }

  def shouldReturnBodyLhasa = {
    val request = sttp.get(uri"http://localhost:8889/data/2.5/weather?q=Lhasa,cn&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.unsafeBody !== ""
  }
}

