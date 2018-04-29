package info.maalvarez.wiremock.mappingloader

import java.util

import com.github.tomakehurst.wiremock.standalone.MappingsSource
import com.github.tomakehurst.wiremock.stubbing.{StubMapping, StubMappings}

class UnitFileMappingSource extends MappingsSource {
  override def save(list: util.List[StubMapping]): Unit = ()

  override def save(stubMapping: StubMapping): Unit = ()

  override def remove(stubMapping: StubMapping): Unit = ()

  override def removeAll(): Unit = ()

  override def loadMappingsInto(stubMappings: StubMappings): Unit = ()
}

object UnitFileMappingSource {
  def apply(): UnitFileMappingSource = new UnitFileMappingSource()
}