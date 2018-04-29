package info.maalvarez.wiremock.mappingloader

import java.nio.file.{Files, Paths}
import java.util
import java.util.UUID

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.tomakehurst.wiremock.common.TextFile
import com.github.tomakehurst.wiremock.standalone.MappingsSource
import com.github.tomakehurst.wiremock.stubbing.{StubMapping, StubMappings}
import com.google.common.base.Charsets

class MultiJsonFileMappingSource(fileName: String) extends MappingsSource {
  private val file: TextFile = new TextFile(Paths.get(s"src/test/resources/mappings/$fileName").toUri)
  private val stubMappingSet: scala.collection.mutable.Set[UUID] = scala.collection.mutable.Set.empty

  private val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.setSerializationInclusion(Include.NON_NULL)

  override def save(stubMappings: util.List[StubMapping]): Unit = stubMappings.forEach(save(_))

  override def save(stubMapping: StubMapping): Unit = {
    var stubMappingSeq: Seq[StubMapping] = parseFile()

    if (stubMappingSet.contains(stubMapping.getId)) {
      stubMappingSeq = stubMappingSeq.filter(elem => elem.getId != stubMapping.getId)
    }

    val content: String = toJsonString(stubMappingSeq :+ stubMapping)

    Files.write(Paths.get(file.getPath), content.getBytes(Charsets.UTF_8))

    stubMappingSet += stubMapping.getId()
    stubMapping.setDirty(false)
  }

  override def remove(stubMapping: StubMapping): Unit = {
    stubMappingSet -= stubMapping.getId

    if (stubMappingSet.isEmpty) {
      Files.delete(Paths.get(file.getPath))
    } else {
      var stubMappingSeq: Seq[StubMapping] = parseFile()

      if (stubMappingSet.contains(stubMapping.getId)) {
        stubMappingSeq = stubMappingSeq.filter(elem => elem.getId != stubMapping.getId)

        val content: String = toJsonString(stubMappingSeq)

        Files.write(Paths.get(file.getPath), content.getBytes(Charsets.UTF_8))
      }
    }
  }

  override def removeAll(): Unit = {
    Files.delete(Paths.get(file.getPath))

    stubMappingSet.clear()
  }

  override def loadMappingsInto(stubMappings: StubMappings): Unit =
    if (Files.exists(Paths.get(file.getPath))) {
      parseFile()
        .foreach(mapping => {
          mapping.setDirty(false)

          stubMappings.addMapping(mapping)

          stubMappingSet += mapping.getId()
        })
    }

  private def parseFile(): Seq[StubMapping] =
    mapper.readValue[Seq[StubMapping]](file.readContentsAsString(), new TypeReference[Seq[StubMapping]](){})

  private def toJsonString(value: Seq[StubMapping]): String =
    mapper.writeValueAsString(value)
}

object MultiJsonFileMappingSource {
  def apply(filePath: String): MultiJsonFileMappingSource = new MultiJsonFileMappingSource(filePath)
}