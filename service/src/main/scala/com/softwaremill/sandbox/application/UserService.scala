package com.softwaremill.sandbox.application

import java.util.UUID

import akka.actor.ActorSystem
import com.softwaremill.sandbox.application.UserActor.UserRegion

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._

class UserService(userRegion: UserRegion)(implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  private implicit val timeout = Timeout(10.seconds)
  private val random = new Random()

  def createUser(uuid: UUID): Future[String] = {
    val uniqueName = "andrzej" + random.nextInt(100)
    (userRegion ? UserActor.CreateUser(uuid.toString, uniqueName)).mapTo[String]
  }

  def getUser(uuid: UUID): Future[Option[String]] = {
    (userRegion ? UserActor.GetUser(uuid.toString)).mapTo[Option[String]]
  }
}
