package com.softwaremill.sandbox.application

import akka.actor.{ActorRef, DiagnosticActorLogging, Status}
import akka.cluster.sharding.ShardRegion.HashCodeMessageExtractor
import akka.event.Logging.MDC
import akka.persistence.PersistentActor
import com.softwaremill.sandbox.application.UserActor.{CreateUser, GetUser, UserCreated}
import com.softwaremill.tagging.@@
import com.typesafe.config.Config
import kamon.trace.Tracer

import scala.util.Random

case class UserActorConfig(config: Config) {

  lazy val userCreationLag = config.getConfig("app").getInt("user-creation-lag")
}

case class UserState(name: String)

class UserActor(userActorConfig: UserActorConfig) extends PersistentActor with DiagnosticActorLogging {

  private val random = new Random()
  var userState: Option[UserState] = None

  override def receiveRecover: Receive = {
    case event: UserCreated => userState = Some(UserState(event.name))
  }
  override def receiveCommand: Receive = {
    case command: CreateUser =>
      val currentSender = sender()
      log.debug("creating user [token {}]", Tracer.currentContext.token)

      validationLogic

      persist(UserCreated(command.name)) { e =>
        userState = Some(UserState(e.name))
        log.debug("user created [token {}]", Tracer.currentContext.token)

        postCreationProcessing

        currentSender ! Status.Success(e.name)
      }

    case command: GetUser =>
      Thread.sleep(random.nextInt(500))
      log.debug("getting user [token {}]", Tracer.currentContext.token)
      sender() ! userState.map(_.name)
  }

  private def postCreationProcessing = {
    val segment = Tracer.currentContext.startSegment("post-creation-processing", "business-logic", "xyz")
    Thread.sleep(random.nextInt(500))
    segment.finish()
  }

  private def validationLogic = {
    val segment = Tracer.currentContext.startSegment("external-validation-service", "validation-logic", "xyz")
    Thread.sleep(random.nextInt(userActorConfig.userCreationLag))
    segment.finish()
  }

  override def mdc(currentMessage: Any): MDC = {
    Map("persistenceId" -> persistenceId)
  }

  override def persistenceId: String = s"UA-${self.path.name}"
}

trait UserCommand {
  def userId: String
}

object UserActor {
  case class CreateUser(userId: String, name: String) extends UserCommand
  case class GetUser(userId: String) extends UserCommand
  case class UserCreated(name: String)

  trait UserRegionTag

  type UserRegion = ActorRef @@ UserRegionTag
}

class UserActorMessageExtractor extends HashCodeMessageExtractor(10) {

  override def entityId(message: Any): String = message match {
    case command: UserCommand => command.userId
  }
}
