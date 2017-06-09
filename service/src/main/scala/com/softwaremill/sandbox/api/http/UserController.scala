package com.softwaremill.sandbox.api.http

import java.util.UUID

import akka.http.scaladsl.server.Directives.{complete, onSuccess, pathEnd, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import com.softwaremill.sandbox.application.UserService
import com.typesafe.scalalogging.LazyLogging
import kamon.akka.http.KamonTraceDirectives
import kamon.trace.Tracer

import scala.concurrent.ExecutionContext

class UserController(userService: UserService)(implicit executionContext: ExecutionContext) extends KamonTraceDirectives with LazyLogging {

  def routes: Route = pathPrefix("user") {
    pathEnd {
      post {
        traceName("user-creation") {
          val uuid = UUID.randomUUID()
          logger.debug(s"processing user $uuid creation request [token ${Tracer.currentContext.token}]")

          onSuccess(userService.createUser(uuid)) {
            case name: String =>
              logger.debug(s"creating user $uuid finished [token ${Tracer.currentContext.token}]")
              complete(201, s"user $uuid created")
          }
        }
      }
    } ~
      pathPrefix(JavaUUID) { uuid =>
        pathEnd {
          get {
            traceName("get-user") {
              onSuccess(userService.getUser(uuid)) {
                case Some(userName) =>
                  logger.debug(s"getting user $uuid finished [token ${Tracer.currentContext.token}]")
                  complete(200, s"user $uuid found, name is: $userName")
                case None =>
                  logger.debug(s"user $uuid not found [token ${Tracer.currentContext.token}]")
                  complete(404, s"user $uuid not found")
              }
            }
          }
        }
      }
  }
}
