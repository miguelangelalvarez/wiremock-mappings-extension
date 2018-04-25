package info.maalvarez

import java.nio.file.{Files, Paths}
import java.util
import java.util.UUID

import com.github.tomakehurst.wiremock.common.{Json, TextFile}
import com.github.tomakehurst.wiremock.standalone.MappingsSource
import com.github.tomakehurst.wiremock.stubbing.{StubMapping, StubMappings}
import com.google.common.base.Charsets

class MultiJsonFileMappingSource(fileName: String) extends MappingsSource {
  private val file: TextFile = new TextFile(getClass.getClassLoader.getResource(s"mappings/$fileName").toURI)
  private val stubMappingSet: scala.collection.mutable.Set[UUID] = scala.collection.mutable.Set.empty

  override def save(stubMappings: util.List[StubMapping]): Unit = stubMappings.forEach(save(_))

  override def save(stubMapping: StubMapping): Unit = {
    var stubMappingSeq: Seq[StubMapping] = Json.read[Seq[StubMapping]](file.readContentsAsString(), classOf[Seq[StubMapping]])

    if (stubMappingSet.contains(stubMapping.getId)) {
      stubMappingSeq = stubMappingSeq.filter(elem => elem.getId != stubMapping.getId)
    }

    val content: String = Json.write[Seq[StubMapping]](stubMappingSeq :+ stubMapping, classOf[Seq[StubMapping]])

    Files.write(Paths.get(file.getPath), content.getBytes(Charsets.UTF_8))

    stubMappingSet += stubMapping.getId()
    stubMapping.setDirty(false)
  }

  override def remove(stubMapping: StubMapping): Unit = {
    stubMappingSet -= stubMapping.getId

    if (stubMappingSet.isEmpty) Files.delete(Paths.get(file.getPath))
  }

  override def removeAll(): Unit = {
    Files.delete(Paths.get(file.getPath))

    stubMappingSet.clear()
  }

  override def loadMappingsInto(stubMappings: StubMappings): Unit =
    if (Files.exists(Paths.get(file.getPath))) {
      splitFileContent(file.readContentsAsString()).foreach(mapping => {
        mapping.setDirty(false)

        stubMappings.reset()
        stubMappings.addMapping(mapping)

        stubMappingSet += mapping.getId()
      })
    }

  private def splitFileContent(content: String): Seq[StubMapping] = Json.read[Seq[StubMapping]](content, classOf[Seq[StubMapping]])
}

object MultiJsonFileMappingSource {
  def apply(filePath: String): MultiJsonFileMappingSource = new MultiJsonFileMappingSource(filePath)
}