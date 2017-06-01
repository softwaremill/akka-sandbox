import sbt._
import Keys._

// Create a new MergeStrategy for aop.xml files
// from https://github.com/kamon-io/Kamon/blob/master/kamon-examples/kamon-autoweave-example/build.sbt#L28-L62
// https://stackoverflow.com/questions/39554672/aspectjweaver-with-fat-jar
val aopMerge = new sbtassembly.MergeStrategy {
  val name = "aopMerge"
  import scala.xml._
  import scala.xml.dtd._

  def apply(tempDir: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] = {
    val dt = DocType("aspectj", PublicID("-//AspectJ//DTD//EN", "http://www.eclipse.org/aspectj/dtd/aspectj.dtd"), Nil)
    val file = MergeStrategy.createMergeTarget(tempDir, path)
    val xmls: Seq[Elem] = files.map(XML.loadFile)
    val aspectsChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "aspects" \ "_")
    val weaverChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "weaver" \ "_")
    val options: String = xmls.map(x => (x \\ "aspectj" \ "weaver" \ "@options").text).mkString(" ").trim
    val weaverAttr = if (options.isEmpty) Null else new UnprefixedAttribute("options", options, Null)
    val aspects = new Elem(null, "aspects", Null, TopScope, false, aspectsChildren: _*)
    val weaver = new Elem(null, "weaver", weaverAttr, TopScope, false, weaverChildren: _*)
    val aspectj = new Elem(null, "aspectj", Null, TopScope, false, aspects, weaver)
    XML.save(file.toString, aspectj, "UTF-8", xmlDecl = false, dt)
    IO.append(file, IO.Newline.getBytes(IO.defaultCharset))
    Right(Seq(file -> path))
  }
}

lazy val commonSettings = Seq(
  organization := "com.softwaremill",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.properties") => MergeStrategy.discard
    case PathList("META-INF", "aop.xml") => aopMerge
    case PathList(ps @ _ *) if ps.last endsWith ".txt.1" => MergeStrategy.first
    case "reference.conf" => MergeStrategy.concat
    case "application.conf" => MergeStrategy.concat
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val sandbox = (project in file("."))
  .aggregate(service)
  .settings(commonSettings)
  .settings(
    run := {
      (run in service in Compile).evaluated // Enables "sbt run" on the root project
    }
  )

val akkaVersion = "2.4.16"
val akkaHttpVersion = "10.0.3"
lazy val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.11.0",
  "ch.megard" %% "akka-http-cors" % "0.1.10",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.18" exclude ("org.scalatest", "scalatest")
)

val circeVersion = "0.6.1"
lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val macwireVersion = "2.2.5"
lazy val macwireDependencies = Seq(
  "com.softwaremill.macwire" %% "macros" % macwireVersion,
  "com.softwaremill.macwire" %% "util" % macwireVersion,
  "com.softwaremill.common" %% "tagging" % "2.0.0"
)

lazy val loggerDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "ch.qos.logback" % "logback-core" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.7",
  "org.codehaus.janino" % "janino" % "2.6.1"
)

val kamonVersion = "0.6.6"
lazy val monitoringDependencies = Seq(
  "io.kamon" %% "kamon-core" % kamonVersion,
  "io.kamon" %% "kamon-jmx" % kamonVersion,
  "io.kamon" %% "kamon-akka-2.4" % kamonVersion,
  "io.kamon" %% "kamon-autoweave" % "0.6.5",
  "io.kamon" %% "kamon-akka-remote-2.4" % kamonVersion,
  "io.kamon" %% "kamon-akka-http" % kamonVersion,
  "io.kamon" %% "kamon-scala" % kamonVersion,
  "io.kamon" %% "kamon-datadog" % kamonVersion
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.1",
  "com.ironcorelabs" %% "cats-scalatest" % "2.1.1",
  "org.mockito" % "mockito-core" % "1.10.19",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "net.manub" %% "scalatest-embedded-kafka" % "0.12.0",
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.18" exclude ("org.scalatest", "scalatest")
).map(_ % Test)

lazy val service = (project in file("service"))
  .settings(commonSettings)
  .settings(
    mainClass in assembly := Some("com.softwaremill.sandbox.Main"),
    mainClass in Compile := Some("com.softwaremill.sandbox.Main"),
    libraryDependencies ++=
      loggerDependencies ++
        akkaDependencies ++
        circeDependencies ++
        macwireDependencies ++
        monitoringDependencies ++
        testDependencies
  )
  .settings(
    parallelExecution in Test := false
  )
  .settings(
    name := "sandbox-service"
  )

lazy val perfTest = (project in file("perf-test"))
  .enablePlugins(GatlingPlugin)
  .settings(
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.0" % "test,it",
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.3" % "test,it",
      "io.gatling" % "gatling-test-framework" % "2.2.3" % "test,it"
    )
  )

scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))
onLoad in Global := (Command.process("scalafmt", _: State)) compose (onLoad in Global).value
