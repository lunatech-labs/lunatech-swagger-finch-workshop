import scala.sys.process._
import sbt._
import Keys._

scalafixSettings
sbtfixSettings // enable semanticdb-sbt for sbt metabuilds.

name := "SkillsAPI"
version := "1.0.2"
scalaVersion := "2.12.4"

lazy val finagleVersion    = "18.2.0"
lazy val finchVersion      = "0.17.0"
lazy val catsVersion       = "0.9.0"
lazy val circeVersion      = "0.9.0"
lazy val scalaCheckVersion = "1.13.4"
lazy val scalaMetaVersion  = "3.0.0"
lazy val scalaTestVersion  = "3.0.1"

val rootDependencies = Seq(
  "com.twitter"        %% "finagle-http"   % finagleVersion,
  "io.circe"           %% "circe-core"     % circeVersion,
  "io.circe"           %% "circe-generic"  % circeVersion,
  "io.circe"           %% "circe-parser"   % circeVersion,
  "com.github.finagle" %% "finch-core"     % finchVersion,
  "com.github.finagle" %% "finch-circe"    % finchVersion,
  "com.twitter"        %% "twitter-server" % finagleVersion,
  "com.typesafe"       % "config"          % "1.3.3",
  "com.typesafe.slick" %% "slick"          % "3.2.1",
  "com.h2database"     % "h2"              % "1.4.196",
  "joda-time"          % "joda-time"       % "2.9.9",
  "ch.qos.logback"     % "logback-classic" % "1.2.3"
)

val testDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion,
  "org.scalatest"  %% "scalatest"  % scalaTestVersion
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-target:jvm-1.8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-numeric-widen",
  "-Yrangepos",
  "-Xplugin-require:semanticdb",
  "-Ypartial-unification",
  "-P:semanticdb:sourceroot:/x",
  "-Xfuture",
  "-Xlint"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

lazy val root =
  project
    .in(file("."))
    .settings(
      scalacOptions ++= compilerOptions,
      libraryDependencies ++= Seq(
        "org.scala-lang"                              % "scala-reflect" % scalaVersion.value
      ) ++ rootDependencies ++ testDependencies.map(_ % "test")
    )
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

scalafmtConfig in ThisBuild := file(".scalafmt.conf")

scalafixConfigure(Compile, Test, IntegrationTest)

addCommandAlias("lint", "all compile:scalafix test:scalafix")

scalafmtOnCompile := true
