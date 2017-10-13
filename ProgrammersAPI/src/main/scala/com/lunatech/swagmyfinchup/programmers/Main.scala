package com.lunatech.swagmyfinchup.programmers

import com.lunatech.swagmyfinchup.programmers.controllers.SqlController
import com.lunatech.swagmyfinchup.programmers.utils.ServerFactory
import com.lunatech.swagmyfinchup.programmers.views.ProgrammersAPI
import com.twitter.app.Flag
import com.twitter.finagle.{ListeningServer, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import org.slf4j.Logger
import org.slf4j.LoggerFactory._

object Main extends TwitterServer with ProgrammersAPI {

  val logger: Logger = getLogger(getClass)

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

    val server: ListeningServer = ServerFactory("ProgrammersAPI", statsReceiver, port, api, None)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }

}
