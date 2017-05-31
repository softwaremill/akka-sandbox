package com.softwaremill.sandbox

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocolBuilder

class BaseSimulation extends Simulation {

  val baseURL = "http://localhost:9000"

  val httpConf: HttpProtocolBuilder = http
    .baseURL(baseURL)
    .acceptHeader("application/json, text/html, text/plain, */*")
    .acceptEncodingHeader("gzip, deflate")
}
