package com.lunatech.swagmyfinchup.programmers

import javax.net.ssl.SSLContext

import com.lunatech.swagmyfinchup.programmers.controllers.SqlController
import com.lunatech.swagmyfinchup.programmers.utils.{ServerFactory, TLSFactory}
import com.lunatech.swagmyfinchup.programmers.views.ProgrammersAPI
import com.twitter.app.Flag
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
import com.twitter.finagle.{ListeningServer, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

object Main extends TwitterServer with ProgrammersAPI {

  val port: Flag[Int]    = flag("port", 8081, "TCP port for HTTP server")
  val tlsPort: Flag[Int] = flag("tlsport", 38080, "TCP port for HTTP server")

  val programmersCounter: Counter = statsReceiver.counter("programmers")

  override def defaultAdminPort: Int = 9081

  val sSLContext: SSLContext =
    TLSFactory.createTlsContext("keystore.jks", "truststore.jks", "17555599")

  val api: Service[Request, Response] = programmersApi
    .handle({
      case e: Exception => NotFound(e)
    })
    .toServiceAs[Application.Json]

  def main(): Unit = {
    logger.info("Serving the ProgrammersAPI")
    SqlController.createDatabase

    val server: ListeningServer = ServerFactory("ProgrammersAPI", statsReceiver, port, api, None)
    val tlsServer: ListeningServer =
      ServerFactory("TLSServer", statsReceiver, tlsPort, api, Some(sSLContext))

    onExit { server.close(); tlsServer.close() }

    Await.ready(adminHttpServer)
  }

}
