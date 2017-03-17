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

object Main extends TwitterServer with ProgrammersAPI {

  val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

  val programmersCounter: Counter = statsReceiver.counter("programmers")

  val api: Service[Request, Response] = programmersApi
    .handle({
      case e: Exception => NotFound(e)
    })
    .toServiceAs[Application.Json]

  def main(): Unit = {
    log.info(s"Serving the ProgrammersAPI on port :${port()}")
    SqlController.createDatabase

    val server = Http.server.withStatsReceiver(statsReceiver).serve(s":${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }

}
