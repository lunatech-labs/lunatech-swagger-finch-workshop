package com.lunatech.swagmyfinchup.programmers

import com.lunatech.swagmyfinchup.programmers.controllers.SqlController
import com.lunatech.swagmyfinchup.programmers.views.ProgrammersAPI
import com.twitter.app.Flag
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.ccadllc.cedi.config.ConfigParser
import com.lunatech.swagmyfinchup.programmers.utils.Helpers._
import com.lunatech.swagmyfinchup.programmers.filters._
import com.typesafe.config.ConfigFactory

object Main extends TwitterServer with ProgrammersAPI with CORSFilter {

  val conf = ConfigFactory.load()

  val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

  val programmersCounter: Counter = statsReceiver.counter("programmers")

  //<Delete>
  override def defaultHttpPort: Int = 9081
  //</Delete>

  val derivedParser: ConfigParser[CorsConfig] = ConfigParser.derived[CorsConfig]
  val corsconfig: CorsConfig                  = validateConfig(derivedParser.under("server.cors").parse(conf))
  val corsFilter: HttpFilter                  = new Cors.HttpFilter(corspolicy(corsconfig))

  val api: Service[Request, Response] = programmersApi
    .handle({
      case e: Exception => NotFound(e)
    })
    .toServiceAs[Application.Json]

  val corsFiltered: Service[Request, Response] = corsFilter andThen api

  def main(): Unit = {
    log.info("Serving the ProgrammersAPI")

    SqlController.createDatabase

    val server = Http.server
      .withLabel("ProgrammersAPI")
      .withStatsReceiver(statsReceiver)
      .serve(s":${port()}", corsFiltered)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }

}
