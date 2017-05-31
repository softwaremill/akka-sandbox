package com.softwaremill.sandbox.api.http

import com.typesafe.config.Config

class ApiConfig(val config: Config) {
  private val apiConfig = config.getConfig("api")

  lazy val host: String = apiConfig.getString("host")
  lazy val port: Int = apiConfig.getInt("port")
  lazy val version: String = apiConfig.getString("version")
}
