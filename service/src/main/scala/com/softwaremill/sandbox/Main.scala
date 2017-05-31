package com.softwaremill.sandbox

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import kamon.Kamon

import scala.util.Try

object Main extends App with MainModule {
  Kamon.start()

  lazy val config = ConfigFactory.load()
  implicit lazy val system = ActorSystem("sandbox-actor-system")
  implicit lazy val materializer = ActorMaterializer()(system)
  implicit lazy val executor = system.dispatcher

  def start(): Unit = {
    startApi()
    logger.info(s"Sandbox started")
  }

  def terminate(): Unit = {
    Try(system.shutdown())
    Try(Kamon.shutdown())
  }

  sys.addShutdownHook {
    terminate()
  }

  start()
}
