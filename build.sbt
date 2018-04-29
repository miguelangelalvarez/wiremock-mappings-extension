organization := "info.maalvarez"
name := "wiremock-single-file-location-extension"
version := "1.0.0"
scalaVersion := "2.12.4"

val Versions = new {
  val WireMockVersion = "2.16.0"
  val Specs2Version = "4.1.0"
  val JacksonVersion = "2.9.5"
  val SttpVersion = "1.1.13"
}

val dependencies: Seq[ModuleID] = List (
  "com.github.tomakehurst"        %  "wiremock"             % Versions.WireMockVersion,
  "com.fasterxml.jackson.module"  %% "jackson-module-scala" % Versions.JacksonVersion
)

val testDependencies: Seq[ModuleID] = List(
  "org.specs2"            %% "specs2-core"  % Versions.Specs2Version,
  "com.softwaremill.sttp" %%  "core"        % Versions.SttpVersion
).map(_ % Test)

libraryDependencies ++= dependencies ++ testDependencies