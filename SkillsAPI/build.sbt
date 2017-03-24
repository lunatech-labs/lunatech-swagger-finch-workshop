import sbt._
import Keys._

name := "SkillsAPI"

version := "1.0.0"

scalaVersion := "2.11.8"

scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))

lazy val finagleVersion       = "6.42.0"
lazy val twitterServerVersion = "1.27.0"
lazy val circeVersion         = "0.7.0"
lazy val finchVersion         = "0.13.1"

val rootDependencies = Seq(
  "com.twitter"        %% "finagle-http"   % finagleVersion,
  "com.twitter"        %% "finagle-stats"  % finagleVersion,
  "io.circe"           %% "circe-core"     % circeVersion,
  "io.circe"           %% "circe-generic"  % circeVersion,
  "io.circe"           %% "circe-parser"   % circeVersion,
  "com.github.finagle" %% "finch-core"     % finchVersion,
  "com.github.finagle" %% "finch-circe"    % finchVersion,
  "com.twitter"        %% "twitter-server" % twitterServerVersion,
  "com.typesafe"       % "config"          % "1.3.1",
  "com.typesafe.slick" %% "slick"          % "3.0.0",
  "com.h2database"     % "h2"              % "1.3.175",
  "org.scalacheck"     %% "scalacheck"     % "1.12.5",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

val testDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.5",
  "org.scalatest"  %% "scalatest"  % "2.2.6"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

lazy val root =
  project
    .in(file("."))
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
        //for tomcat webapp
      ) ++ rootDependencies ++ testDependencies.map(_ % "test"))

fork in run := true

reformatOnCompileSettings
