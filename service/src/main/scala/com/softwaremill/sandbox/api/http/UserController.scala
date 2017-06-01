package com.softwaremill.sandbox.api.http

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, onSuccess, pathEnd, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.softwaremill.sandbox.application.UserActor
import com.softwaremill.sandbox.application.UserActor.UserRegion
import kamon.trace.Tracer

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.Random
import akka.pattern.after

class UserController(userRegion: UserRegion)(implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  implicit val timeout = Timeout(10.seconds)
  private val random = new Random()

  def routes: Route = pathPrefix("user") {
    pathEnd {
      post {
        val traceableUserCreation = Tracer.withNewContext("user-creation") {
          (userRegion ? UserActor.CreateUser(UUID.randomUUID().toString, "andrzej" + random.nextInt(100))).mapTo[String].map { result =>
            Tracer.currentContext.finish()
            result
          }
        }
        onSuccess(traceableUserCreation) {
          case name: String => complete(s"created user: $name")
        }
      } ~
        get {
          val pause = random.nextInt(1000)
          val responseMessage = Future.successful(s"user data after: $pause")
          onSuccess(after(pause millis, actorSystem.scheduler)(responseMessage)) {
            case message: String => complete(withRandomCode(message))
          }
        }
    }
  }

  private def withRandomCode(msg: String): StatusCode = {
    val codes = List(200, 201, 202, 203, 304)
    StatusCodes.custom(codes(random.nextInt(5)), msg)
  }
}
