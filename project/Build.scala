import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  val akkaVersion = "2.1.4"

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Fast Food",
      organization := "org.filippodeluca",
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.10.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

        "ch.qos.logback" % "logback-classic" % "1.0.0",

        "junit"             % "junit"           % "4.5"    % "test",
        "org.scalatest"     % "scalatest_2.9.0" % "1.6.1"  % "test"
      )
    )
  )
}
