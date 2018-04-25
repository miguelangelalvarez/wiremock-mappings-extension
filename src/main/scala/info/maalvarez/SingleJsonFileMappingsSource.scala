package info.maalvarez

import java.nio.file.{Files, Paths}
import java.util
import java.util.UUID

import com.github.tomakehurst.wiremock.common.{Json, TextFile}
import com.github.tomakehurst.wiremock.standalone.MappingsSource
import com.github.tomakehurst.wiremock.stubbing.{StubMapping, StubMappings}
import com.google.common.base.Charsets

class SingleJsonFileMappingsSource(fileName: String) extends MappingsSource {
  private val file: TextFile = new TextFile(getClass.getClassLoader.getResource(s"mappings/$fileName").toURI)
  private var fileId: UUID = _

  override def save(stubMappings: util.List[StubMapping]): Unit = stubMappings.forEach(save(_))

  override def save(stubMapping: StubMapping): Unit =
    if (stubMapping.getId == fileId) Files.write(Paths.get(file.getPath), Json.write(stubMapping).getBytes(Charsets.UTF_8))

  override def remove(stubMapping: StubMapping): Unit =
    if (stubMapping.getId == fileId) Files.delete(Paths.get(file.getPath))

  override def removeAll(): Unit = Files.delete(Paths.get(file.getPath))

  override def loadMappingsInto(stubMappings: StubMappings): Unit =
    if (Files.exists(Paths.get(file.getPath))) {
      val mapping: StubMapping = StubMapping.buildFrom(file.readContentsAsString())
      mapping.setDirty(false)

      stubMappings.reset()
      stubMappings.addMapping(mapping)

      fileId = mapping.getId
    }
}

object SingleJsonFileMappingsSource {
  def apply(filePath: String): SingleJsonFileMappingsSource = new SingleJsonFileMappingsSource(filePath)
}