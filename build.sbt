organization := "info.maalvarez"
name := "wiremock-single-file-location-extension"
version := "1.0.0"
scalaVersion := "2.12.4"

val Versions = new {
  val WireMockVersion = "2.16.0"
  val circeVersion = "0.9.0"
}

val dependencies: Seq[ModuleID] = List (
  "com.github.tomakehurst"  %  "wiremock"       % Versions.WireMockVersion
)

libraryDependencies ++= dependencies