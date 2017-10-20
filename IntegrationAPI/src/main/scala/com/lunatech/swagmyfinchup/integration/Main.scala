package com.lunatech.swagmyfinchup.integration


import com.lunatech.swagmyfinchup.integration.controllers.SqlController
import com.lunatech.swagmyfinchup.integration.filters.CORSFilter
import com.lunatech.swagmyfinchup.integration.views.IntegrationAPI
import com.twitter.app.Flag
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{ListeningServer, Name, Resolver, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

import com.lunatech.swagmyfinchup.integration.utils.ServerFactory


object Main
    extends TwitterServer
    with IntegrationAPI
    with CORSFilter {

  val conf = ConfigFactory.load()

  val port: Flag[Int]         = flag("port", 8090, "TCP port for HTTP server")
  val filteredPort: Flag[Int] = flag("port", 8095, "TCP port for HTTP server")
  val tlsPort: Flag[Int]      = flag("port", 38082, "TCP port for HTTPS server")

  override def defaultHttpPort: Int = 9985

  val programmersAPIHost: Name = Resolver.eval(conf.getString("services.programmers.host"))

//  val programmersClient: Service[Request, Response] = ???

  val skillsAPIHost: Name = Resolver.eval(conf.getString("services.skills.host"))

//  val skillsClient: Service[Request, Response] = ???

//  lazy val programmersService = ???
//  lazy val skillsService      = ???

  val api: Service[Request, Response] = integrationApi
    .handle({
      case e: Exception => NotFound(e)
    })
    .toServiceAs[Application.Json]

//  val corsFiltered: Service[Request, Response] = ???

//  val filteredAPI: Service[Request, Response] = ???

  def main(): Unit = {
    log.info("Serving the IntegrationAPI")
    SqlController.createDatabase

    val server: ListeningServer = ServerFactory("MainServer", statsReceiver, port, api, None)
//    val filteredServer: ListeningServer = ???
//    val tlsServer: ListeningServer = ???

    onExit {
      server.close()
//      filteredServer.close()
//      tlsServer.close()
    }

    Await.ready(adminHttpServer)
  }

}
