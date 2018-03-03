package com.lunatech.swagmyfinchup.skills

import javax.net.ssl.SSLContext

import com.lunatech.swagmyfinchup.skills.controllers.SqlController
import com.lunatech.swagmyfinchup.skills.utils.{ServerFactory, TLSFactory}
import com.lunatech.swagmyfinchup.skills.views.SkillsAPI
import com.twitter.app.Flag
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

object Main extends TwitterServer with SkillsAPI {

  val port: Flag[Int]    = flag("port", 8085, "TCP port for HTTP server")
  val tlsPort: Flag[Int] = flag("tlsport", 38081, "TCP port for HTTP server")

  val skillsCounter: Counter = statsReceiver.counter("skills")

  override def defaultAdminPort: Int = 9995

  val sSLContext: SSLContext =
    TLSFactory.createTlsContext("keystore.jks", "truststore.jks", "17555599")

  val api: Service[Request, Response] = skillsApi
    .handle({
      case e: Exception => {
        e.printStackTrace()
        NotFound(e)
      }
    })
    .toServiceAs[Application.Json]

  def main(): Unit = {
    logger.info("Serving the SkillsAPI")
    SqlController.createDatabase

    val server    = ServerFactory("SkillsAPI", statsReceiver, port, api, None)
    val tlsServer = ServerFactory("TLSServer", statsReceiver, tlsPort, api, Some(sSLContext))

    onExit { server.close(); tlsServer.close() }

    Await.ready(adminHttpServer)
  }

}
