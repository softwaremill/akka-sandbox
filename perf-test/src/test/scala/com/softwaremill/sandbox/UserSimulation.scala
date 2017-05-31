package com.softwaremill.sandbox

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class UserSimulation extends BaseSimulation {

  val userScenario = scenario("User scenario")
    .exec(http("create user").post("/user"))

  setUp(
    userScenario
      .inject(rampUsers(100) over(15 seconds) )
      .protocols(httpConf))
    .assertions(forAll.failedRequests.percent.is(0))
}



