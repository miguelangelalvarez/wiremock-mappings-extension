package info.maalvarez

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}
import java.util.UUID

import com.github.tomakehurst.wiremock.stubbing.{InMemoryStubMappings, StubMapping}
import com.google.common.base.Charsets
import org.specs2._
import org.specs2.specification.{BeforeAfterAll, BeforeAfterEach, BeforeAll}
import org.specs2.specification.core.SpecStructure

class SingleJsonFileMappingSourceTest extends Specification with BeforeAll {
  val ResourcePath: String = getClass.getClassLoader.getResource("").getPath + "mappings"
  val TestFileName: String = "single-json-file-mapping-source-test.json"
  val DefaultUuid: UUID = UUID.randomUUID

  override def is: SpecStructure =
    s2"""The single JSON file mapping source should
      | save the stub mapping with the same UUID
      | delete the file when remove is called with an existing stub mapping           $shouldDeleteFileWhenRemoveAnExistingStubMapping
      | not delete the file when remove is called with a non-existing stub mapping    $shouldNotDeleteFileWhenRemoveANonExistingStubMapping
      | delete the file when removeAll is called                                      $shouldDeleteFileWhenRemoveAll
    """.stripMargin

  override def beforeAll: Unit = Files.createDirectories(Paths.get(ResourcePath))

  private def createFile(filename: String): Unit =
    Files.write(
      Paths.get(s"$ResourcePath/$filename"),
      s"""{
         |  "id": "${DefaultUuid.toString}",
         |  "request": {
         |    "method": "GET",
         |    "urlPattern": "/testmapping"
         |  },
         |  "response": {
         |    "status": 200,
         |    "body": "default test mapping",
         |    "headers": {
         |      "Content-Type": "text/plain"
         |    }
         |  }
         |}""".stripMargin.getBytes(Charsets.UTF_8)
    )

  //  def shouldSave

  def shouldDeleteFileWhenRemoveAnExistingStubMapping = {
    createFile(s"1-$TestFileName")

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"1-$TestFileName")
    val stubMapping: StubMapping = new StubMapping()
    stubMapping.setId(DefaultUuid)

    mappingSource.loadMappingsInto(new InMemoryStubMappings())
    mappingSource.remove(stubMapping)

    Files.exists(Paths.get(s"$ResourcePath/1-$TestFileName")) === false
  }

  def shouldNotDeleteFileWhenRemoveANonExistingStubMapping = {
    createFile(s"2-$TestFileName")

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"2-$TestFileName")
    val stubMapping: StubMapping = new StubMapping()

    mappingSource.loadMappingsInto(new InMemoryStubMappings())
    mappingSource.remove(stubMapping)

    Files.exists(Paths.get(s"$ResourcePath/2-$TestFileName")) === true
  }

  def shouldDeleteFileWhenRemoveAll = {
    createFile(s"3-$TestFileName")

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"3-$TestFileName")

    mappingSource.loadMappingsInto(new InMemoryStubMappings())
    mappingSource.removeAll()

    Files.exists(Paths.get(s"$ResourcePath/3-$TestFileName")) === false
  }
}
