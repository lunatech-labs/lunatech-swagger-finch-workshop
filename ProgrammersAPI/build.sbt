
name := "ProgrammersAPI"

version := "1.0.1"

scalaVersion := "2.12.3"

//scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))

lazy val finagleVersion       = "6.45.0"
lazy val twitterServerVersion = "1.30.0"

lazy val catsVersion = "0.9.0"
lazy val circeVersion = "0.8.0"
lazy val finchVersion = "0.15.1"
lazy val logbackVersion = "1.2.3"

val rootDependencies = Seq(
  "com.twitter"        %% "finagle-http"   % finagleVersion,
  "io.circe"           %% "circe-core"     % circeVersion,
  "io.circe"           %% "circe-generic"  % circeVersion,
  "io.circe"           %% "circe-parser"   % circeVersion,
  "com.github.finagle" %% "finch-core"     % finchVersion,
  "com.github.finagle" %% "finch-circe"    % finchVersion,
  "com.twitter"        %% "twitter-server" % twitterServerVersion,
  "com.typesafe"       % "config"          % "1.3.1",
  "com.typesafe.slick" %% "slick"          % "3.2.0",
  "com.h2database"     % "h2"              % "1.3.175",
  "joda-time"          % "joda-time"       % "2.3",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

val testDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.5",
  "org.scalatest"  %% "scalatest"  % "3.0.3"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

lazy val root =
  project
    .in(file("."))
    .settings(
      scalacOptions += "-Ywarn-unused-import" ,
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
        //for tomcat webapp
      ) ++ rootDependencies ++ testDependencies.map(_ % "test"))
    .settings(initialCommands in console :=
      """
        |import io.finch._
        |import io.finch.circe._
        |import io.finch.items._
        |import com.twitter.util.{Future, Await, Try}
        |import com.twitter.concurrent.AsyncStream
        |import com.twitter.io.{Buf, Reader}
        |import com.twitter.finagle.Service
        |import com.twitter.finagle.Http
        |import com.twitter.finagle.http.{Request, Response, Status, Version}
        |import io.circe._
        |import io.circe.generic.auto._
        |import shapeless._
        |import org.joda.time._
      """.stripMargin)

fork in run := true

//reformatOnCompileSettings
