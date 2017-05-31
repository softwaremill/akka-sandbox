package com.softwaremill.sandbox

import java.util.UUID

import akka.actor.Status.Status
import akka.actor.{ActorSystem, Props, Status}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import ch.megard.akka.http.cors.CorsDirectives
import com.softwaremill.macwire._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}
import akka.pattern._
import com.softwaremill.sandbox.api.http.{ApiConfig, UserController}
import com.softwaremill.sandbox.application.{UserActor, UserActorMessageExtractor}
import com.softwaremill.sandbox.application.UserActor.{UserRegion, UserRegionTag}
import com.softwaremill.tagging.Tagger
import kamon.trace.Tracer

trait MainModule extends LazyLogging with CorsDirectives {
  def config: Config
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  private var serverBinding: Option[ServerBinding] = None
  private lazy val apiConfig = wire[ApiConfig]
  private lazy val userActorMessageExtractor = wire[UserActorMessageExtractor]

  private lazy val userRegion: UserRegion =
    ClusterSharding(system)
      .start(
        typeName = "UserActor",
        entityProps = Props[UserActor],
        settings = ClusterShardingSettings(system),
        messageExtractor = userActorMessageExtractor
      )
      .taggedWith[UserRegionTag]

  private lazy val userController = wire[UserController]

  def startApi(): Future[ServerBinding] = {
    logger.info(s"Starting API at: ${apiConfig.host}:${apiConfig.port}")
    Http().bindAndHandle(routes, apiConfig.host, apiConfig.port).andThen {
      case Success(binding) =>
        serverBinding = Some(binding)
        logger.info(s"API ready at ${apiConfig.host}:${apiConfig.port}")
      case Failure(e) => logger.error("Unable to start API", e)
    }
  }

  def stopApi(): Unit = serverBinding.foreach(binding => Await.ready(binding.unbind(), 1.minute))

  def routes: Route =
    requestTime(Logging.InfoLevel, cors() {
      userController.routes
    })

  def requestTime(level: LogLevel, route: Route)(implicit m: Materializer, ex: ExecutionContext) = {

    def elapsedTime(requestTimestamp: Long): Long = {
      val responseTimestamp: Long = System.currentTimeMillis()
      responseTimestamp - requestTimestamp
    }

    def akkaResponseTimeLoggingFunction(loggingAdapter: LoggingAdapter, requestTimestamp: Long)(req: HttpRequest)(res: Any): Unit = {
      val time = elapsedTime(requestTimestamp)
      val entry = LogEntry(s"request: ${req.method} ${req.getUri()}, time=$time[ms]", level)
      entry.logTo(loggingAdapter)
    }
    DebuggingDirectives.logRequestResult(LoggingMagnet(log => {
      val requestTimestamp = System.currentTimeMillis()
      akkaResponseTimeLoggingFunction(log, requestTimestamp)
    }))(route)

  }
}
