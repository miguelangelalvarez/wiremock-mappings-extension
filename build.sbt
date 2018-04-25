organization := "info.maalvarez"
name := "wiremock-single-file-location-extension"
version := "1.0.0"
scalaVersion := "2.12.4"

val Versions = new {
  val WireMockVersion = "2.16.0"
  val Specs2Version = "4.1.0"
}

val dependencies: Seq[ModuleID] = List (
  "com.github.tomakehurst"  % "wiremock"  % Versions.WireMockVersion
)

val testDependencies: Seq[ModuleID] = List(
  "org.specs2"  %% "specs2-core"  % Versions.Specs2Version
).map(_ % Test)

libraryDependencies ++= dependencies ++ testDependencies