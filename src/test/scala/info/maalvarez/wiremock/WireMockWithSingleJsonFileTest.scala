package info.maalvarez.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.softwaremill.sttp._
import info.maalvarez.wiremock.mappingloader.{SingleJsonFileMappingsSource, UnitFileMappingSource}
import org.specs2.Specification
import org.specs2.specification.core.SpecStructure
import org.specs2.specification.{BeforeAfterAll, BeforeEach}

class WireMockWithSingleJsonFileTest extends Specification with BeforeAfterAll with BeforeEach {
  val wireMockConfig: WireMockConfiguration =
    new WireMockConfiguration()
      .port(8888)
      .mappingSource(UnitFileMappingSource())
  val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig)

  implicit val backend = HttpURLConnectionBackend()

  override def is: SpecStructure =
    s2"""The request should
      | return OK                       $shouldReturnOkStatus
      | return the content type         $shouldReturnContentType
      | return a non-empty body         $shouldReturnBody
    """.stripMargin

  override def beforeAll: Unit = wireMockServer.start()

  override def before(): Unit = wireMockServer.resetAll()

  override def afterAll(): Unit = wireMockServer.stop()

  def shouldReturnOkStatus = {
    wireMockServer.loadMappingsUsing(SingleJsonFileMappingsSource("single-request-response.json"))

    val request = sttp.get(uri"http://localhost:8888/data/2.5/weather?q=London,uk&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.is200 == true
  }

  def shouldReturnContentType = {
    wireMockServer.loadMappingsUsing(SingleJsonFileMappingsSource("single-request-response.json"))

    val request = sttp.get(uri"http://localhost:8888/data/2.5/weather?q=London,uk&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.contentType must beSome("application/json")
  }

  def shouldReturnBody = {
    wireMockServer.loadMappingsUsing(SingleJsonFileMappingsSource("single-request-response.json"))

    val request = sttp.get(uri"http://localhost:8888/data/2.5/weather?q=London,uk&appid=b6907d289e10d714a6e88b30761fae22")
    val response = request.send()

    response.unsafeBody !== ""
  }
}
