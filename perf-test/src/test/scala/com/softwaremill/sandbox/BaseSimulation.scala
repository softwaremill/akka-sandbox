package com.softwaremill.sandbox

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef.{http, regex, status}
import io.gatling.http.protocol.HttpProtocolBuilder

class BaseSimulation extends Simulation {

  val localhostURL = "http://localhost:9000"

  val httpConf: HttpProtocolBuilder = createHttpConf(localhostURL)

  val host1HttpConf: HttpProtocolBuilder = createHttpConf("http://192.168.69.100:9000")
  val host2HttpConf: HttpProtocolBuilder = createHttpConf("http://192.168.69.101:9000")

  private def createHttpConf(baseUrl: String) =
    http
      .baseURL(baseUrl)
      .acceptHeader("application/json, text/html, text/plain, */*")
      .acceptEncodingHeader("gzip, deflate")

  val userCreationChain = exec(
    http("create user")
      .post("/user")
      .check(status.is(201))
      .check(saveUserId))
    .exec(http("get user").get("/user/${userId}").check(status.is(200)))

  private def saveUserId = {
    regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").saveAs("userId")
  }
}
