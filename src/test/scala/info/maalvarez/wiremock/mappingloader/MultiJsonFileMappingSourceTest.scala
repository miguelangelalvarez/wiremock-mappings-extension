package info.maalvarez.wiremock.mappingloader

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.tomakehurst.wiremock.stubbing.{InMemoryStubMappings, StubMapping}
import com.google.common.base.Charsets
import org.specs2._
import org.specs2.specification.BeforeAfterAll
import org.specs2.specification.core.SpecStructure

import scala.collection.JavaConverters._

class MultiJsonFileMappingSourceTest extends Specification with BeforeAfterAll {
  val ResourcePath: Path = Paths.get(s"src/test/resources/mappings/multi")
  val DefaultUuids: List[UUID] = List.fill[UUID](2)(UUID.randomUUID())

  val Mapper: ObjectMapper = new ObjectMapper()
  Mapper.registerModule(DefaultScalaModule)
  Mapper.setSerializationInclusion(Include.NON_NULL)

  override def is: SpecStructure =
    s2"""The multi JSON file mapping should
      | add some stub mappings if they do not exist                                   $shouldAddSomeStubMappingsIfTheyDoNotExist
      | modify the file if there is a stub mapping with the same id                   $shouldModifyTheFileIfThereIsAStubMappingWithTheSameId
      | add a stub mapping if it does not exist                                       $shouldAddAStubMappingIfItDoesNotExist
      | modify the file if the stub mapping has the same id                           $shouldModifyTheFileIfTheStubMappingHasTheSameId
      | delete the file when remove is called with the last stub mapping              $shouldDeleteFileWhenRemoveLastStubMapping
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

    val mappingSource: MultiJsonFileMappingSource = MultiJsonFileMappingSource(s"multi/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping1: StubMapping = new StubMapping()
    stubMapping1.setId(DefaultUuids(0))
    stubMapping1.setName("stubMapping1")
    val stubMapping2: StubMapping = new StubMapping()
    stubMapping2.setId(UUID.randomUUID())
    stubMapping2.setName("stubMapping2")

    mappingSource.save(List(stubMapping1, stubMapping2).asJava)

    val content: Seq[StubMapping] = parseFile(Paths.get(s"$ResourcePath/$fileName"))

    content.filterNot(elem => elem.getName === stubMapping1.getName).isEmpty == false
  }

  def shouldAddSomeStubMappingsIfTheyDoNotExist = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val path: Path = Paths.get(s"$ResourcePath/$fileName")

    val oldContent: Seq[StubMapping] = parseFile(path)

    val mappingSource: MultiJsonFileMappingSource = MultiJsonFileMappingSource(s"multi/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping1: StubMapping = new StubMapping()
    stubMapping1.setId(UUID.randomUUID())
    stubMapping1.setName("stubMapping1")
    val stubMapping2: StubMapping = new StubMapping()
    stubMapping2.setId(UUID.randomUUID())
    stubMapping2.setName("stubMapping2")

    val stubMappingList: List[StubMapping] = List(stubMapping1, stubMapping2)

    mappingSource.save(stubMappingList.asJava)

    val newContent: Seq[StubMapping] = parseFile(path)

    newContent.size === oldContent.size + stubMappingList.size
  }

  def shouldModifyTheFileIfTheStubMappingHasTheSameId = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val mappingSource: MultiJsonFileMappingSource = MultiJsonFileMappingSource(s"multi/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping: StubMapping = new StubMapping()
    stubMapping.setId(DefaultUuids(0))
    stubMapping.setName("new-one")

    mappingSource.save(stubMapping)

    val content: Seq[StubMapping] = parseFile(Paths.get(s"$ResourcePath/$fileName"))

    content.filterNot(elem => elem.getName === stubMapping.getName).isEmpty == false
  }

  def shouldAddAStubMappingIfItDoesNotExist = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val path: Path = Paths.get(s"$ResourcePath/$fileName")

    val oldContent: Seq[StubMapping] = parseFile(path)

    val mappingSource: MultiJsonFileMappingSource = MultiJsonFileMappingSource(s"multi/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping: StubMapping = new StubMapping()
    stubMapping.setId(UUID.randomUUID())
    stubMapping.setName("new-one")

    mappingSource.save(stubMapping)

    val newContent: Seq[StubMapping] = parseFile(path)

    newContent.size === oldContent.size + 1
  }

  def shouldDeleteFileWhenRemoveLastStubMapping = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val mappingSource: MultiJsonFileMappingSource = MultiJsonFileMappingSource(s"multi/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    DefaultUuids.foreach(uuid => {
      val stubMapping: StubMapping = new StubMapping()
      stubMapping.setId(uuid)

      mappingSource.remove(stubMapping)
    })

    Files.exists(Paths.get(s"$ResourcePath/$fileName")) === false
  }

  def shouldNotDeleteFileWhenRemoveANonExistingStubMapping = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val mappingSource: MultiJsonFileMappingSource = MultiJsonFileMappingSource(s"multi/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())

    val stubMapping: StubMapping = new StubMapping()

    mappingSource.remove(stubMapping)

    Files.exists(Paths.get(s"$ResourcePath/$fileName")) === true
  }

  def shouldDeleteFileWhenRemoveAll = {
    val fileName: String = UUID.randomUUID().toString

    createFile(fileName)

    val mappingSource: MultiJsonFileMappingSource = MultiJsonFileMappingSource(s"multi/$fileName")
    mappingSource.loadMappingsInto(new InMemoryStubMappings())
    mappingSource.removeAll()

    Files.exists(Paths.get(s"$ResourcePath/$fileName")) === false
  }

  private def createFile(filename: String) =
    Files.write(
      Paths.get(s"$ResourcePath/$filename"),
      DefaultUuids.map(uuid =>
        s"""{
         |  "id": "${uuid.toString}",
         |  "request": {
         |    "method": "GET",
         |    "urlPattern": "/testmapping/${uuid.toString}"
         |  },
         |  "response": {
         |    "status": 200,
         |    "body": "default test mapping",
         |    "headers": {
         |      "Content-Type": "text/plain"
         |    }
         |  }
         |}""".stripMargin
      ).mkString("[", ", ", "]").getBytes(Charsets.UTF_8)
    )

  private def parseFile(path: Path): Seq[StubMapping] =
    Mapper.readValue[Seq[StubMapping]](getFileContent(path), new TypeReference[Seq[StubMapping]](){})

  private def getFileContent(path: Path): String =
    Files.readAllLines(path, Charsets.UTF_8)
      .asScala.foldLeft("")(_ + _)
}
