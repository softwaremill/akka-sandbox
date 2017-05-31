package com.softwaremill.sandbox

import io.gatling.core.Predef.{forAll, rampUsers, scenario}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class GetUserSimulation extends BaseSimulation{

  val userScenario = scenario("Get user scenario")
    .exec(http("get user").get("/user"))

  setUp(
    userScenario
      .inject(rampUsers(100) over(1 minute) )
      .protocols(httpConf))
    .assertions(forAll.failedRequests.percent.is(0))
}
