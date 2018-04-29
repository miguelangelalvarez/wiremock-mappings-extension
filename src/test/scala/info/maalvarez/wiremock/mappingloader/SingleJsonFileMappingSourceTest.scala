package info.maalvarez.wiremock.mappingloader

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.github.tomakehurst.wiremock.stubbing.{InMemoryStubMappings, StubMapping}
import com.google.common.base.Charsets
import org.specs2._
import org.specs2.specification.BeforeAfterAll
import org.specs2.specification.core.SpecStructure

import scala.collection.JavaConverters._

class SingleJsonFileMappingSourceTest extends Specification with BeforeAfterAll {
  val ResourcePath: Path = Paths.get(s"src/test/resources/mappings/single")
  val DefaultUuid: UUID = UUID.randomUUID

  override def is: SpecStructure =
    s2"""The single JSON file mapping should
        | not modify the file if there are not any stub mapping with the same id        $shouldNotModifyTheFileIfThereAreNotAnyStubMappingWithTheSameId
        | modify the file if there is a stub mapping with the same id                   $shouldModifyTheFileIfThereIsAStubMappingWithTheSameId
        | not modify the file if the stub mapping does not have the same id             $shouldNotModifyTheFileIfTheStubMappingDoesNotHaveTheSameId
        | modify the file if the stub mapping has the same id                           $shouldModifyTheFileIfTheStubMappingHasTheSameId
        | delete the file when remove is called with an existing stub mapping           $shouldDeleteFileWhenRemoveAnExistingStubMapping
        | not delete the file when remove is called with a non-existing stub mapping    $shouldNotDeleteFileWhenRemoveANonExistingStubMapping
        | delete the file when removeAll is called                                      $shouldDeleteFileWhenRemoveAll
    """.stripMargin

  override def beforeAll: Unit = Files.createDirectories(ResourcePath)

  override def afterAll: Unit = {
    ResourcePath.toFile.listFiles().foreach(file => file.delete())

    Files.delete(ResourcePath)
  }

  def shouldModifyTheFileIfThereIsAStubMappingWithTheSameId = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val path: Path = Paths.get(s"$ResourcePath/$fileName")

    val oldContent: String = getFileContent(path)

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"single/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping1: StubMapping = new StubMapping()
    stubMapping1.setId(DefaultUuid)
    stubMapping1.setName("stubMapping1")
    val stubMapping2: StubMapping = new StubMapping()
    stubMapping2.setId(UUID.randomUUID())
    stubMapping2.setName("stubMapping2")

    mappingSource.save(List(stubMapping1, stubMapping2).asJava)

    val newContent: String = getFileContent(path)

    oldContent !== newContent
  }

  def shouldNotModifyTheFileIfThereAreNotAnyStubMappingWithTheSameId = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val path: Path = Paths.get(s"$ResourcePath/$fileName")

    val oldContent: String = getFileContent(path)

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"single/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping1: StubMapping = new StubMapping()
    stubMapping1.setId(UUID.randomUUID())
    stubMapping1.setName("stubMapping1")
    val stubMapping2: StubMapping = new StubMapping()
    stubMapping2.setId(UUID.randomUUID())
    stubMapping2.setName("stubMapping2")

    mappingSource.save(List(stubMapping1, stubMapping2).asJava)

    val newContent: String = getFileContent(path)

    oldContent === newContent
  }

  def shouldModifyTheFileIfTheStubMappingHasTheSameId = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val path: Path = Paths.get(s"$ResourcePath/$fileName")

    val oldContent: String = getFileContent(path)

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"single/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping: StubMapping = new StubMapping()
    stubMapping.setId(DefaultUuid)
    stubMapping.setName("new-one")

    mappingSource.save(stubMapping)

    val newContent: String = getFileContent(path)

    oldContent !== newContent
  }

  def shouldNotModifyTheFileIfTheStubMappingDoesNotHaveTheSameId = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val path: Path = Paths.get(s"$ResourcePath/$fileName")

    val oldContent: String = getFileContent(path)

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"single/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping: StubMapping = new StubMapping()
    stubMapping.setId(UUID.randomUUID())
    stubMapping.setName("new-one")

    mappingSource.save(stubMapping)

    val newContent: String = getFileContent(path)

    oldContent === newContent
  }

  def shouldDeleteFileWhenRemoveAnExistingStubMapping = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"single/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping: StubMapping = new StubMapping()
    stubMapping.setId(DefaultUuid)

    mappingSource.remove(stubMapping)

    Files.exists(Paths.get(s"$ResourcePath/$fileName")) === false
  }

  def shouldNotDeleteFileWhenRemoveANonExistingStubMapping = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"single/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping: StubMapping = new StubMapping()

    mappingSource.remove(stubMapping)

    Files.exists(Paths.get(s"$ResourcePath/$fileName")) === true
  }

  def shouldDeleteFileWhenRemoveAll = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val mappingSource: SingleJsonFileMappingsSource = SingleJsonFileMappingsSource(s"single/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())
    mappingSource.removeAll()

    Files.exists(Paths.get(s"$ResourcePath/$fileName")) === false
  }

  private def createFile(filename: String) =
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

  private def getFileContent(path: Path) =
    Files.readAllLines(path, Charsets.UTF_8)
      .asScala.foldLeft("")(_ + _)
}
