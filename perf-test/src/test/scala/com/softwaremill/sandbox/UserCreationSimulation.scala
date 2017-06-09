package com.softwaremill.sandbox

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class UserCreationSimulation extends BaseSimulation {

  setUp(
    scenario("User creation").exec(userCreationChain)
      .inject(rampUsers(1) over (120 seconds))
      .protocols(httpConf))
    .assertions(forAll.failedRequests.percent.is(0))
}
