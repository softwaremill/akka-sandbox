logLevel := Level.Warn

resolvers ++= Seq(
  Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
)

addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % "1.0.1")

addSbtPlugin("com.geirsson" %% "sbt-scalafmt" % "0.4.10")

addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.1")
