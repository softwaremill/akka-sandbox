package com.softwaremill.sandbox

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class UserCreationInClusterSimulation extends BaseSimulation {

  setUp(
    scenario("User creation host1").exec(userCreationChain)
      .inject(rampUsers(500) over(240 seconds) )
      .protocols(host1HttpConf),
    scenario("User creation host2").exec(userCreationChain)
      .inject(rampUsers(500) over(240 seconds) )
      .protocols(host2HttpConf))
    .assertions(forAll.failedRequests.percent.is(0))
}



