package com.lunatech.swagmyfinchup.integration

import com.lunatech.swagmyfinchup.integration.controllers.impl.{ProgrammersService, SkillsService}
import com.lunatech.swagmyfinchup.integration.controllers.{APIService, SqlController}
import com.lunatech.swagmyfinchup.integration.filters.{CORSFilter, HeaderValidator}
import com.lunatech.swagmyfinchup.integration.models.{Programmer, Skill}
import com.lunatech.swagmyfinchup.integration.utils.Helpers.CorsConfig
import com.lunatech.swagmyfinchup.integration.views.{IntegrationAPI, ProgrammersAPI, SkillsAPI}
import com.twitter.app.Flag
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Name, Resolver, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import com.ccadllc.cedi.config.ConfigParser
import com.lunatech.swagmyfinchup.integration.utils.Helpers._
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import javax.net.ssl.SSLContext

import com.lunatech.swagmyfinchup.integration.utils.{ClientFactory, ServerFactory, TLSFactory}

import scala.concurrent.ExecutionContext.Implicits.global

object Main
    extends TwitterServer
    with IntegrationAPI
    with ProgrammersAPI
    with SkillsAPI
    with CORSFilter {

  val conf = ConfigFactory.load()

  val port: Flag[Int]         = flag("port", 8090, "TCP port for HTTP server")
  val filteredPort: Flag[Int] = flag("fileredport", 8095, "TCP port for HTTP server")
  val tlsPort: Flag[Int]      = flag("tlsport", 38082, "TCP port for HTTPS server")

  override def defaultAdminPort: Int = 9985

  val derivedParser: ConfigParser[CorsConfig] = ConfigParser.derived[CorsConfig]
  val corsconfig: CorsConfig                  = validateConfig(derivedParser.under("server.cors").parse(conf))
  val corsFilter: HttpFilter                  = new Cors.HttpFilter(corspolicy(corsconfig))

  val sSLContext: SSLContext =
    TLSFactory.createTlsContext("keystore.jks", "truststore.jks", "17555599")

  val programmersAPIHost: Name = Resolver.eval(conf.getString("services.programmers.host"))

  val programmersClient: Service[Request, Response] =
    ClientFactory("programmers-api", programmersAPIHost, Some(sSLContext))

  val skillsAPIHost: Name = Resolver.eval(conf.getString("services.skills.host"))

  val skillsClient: Service[Request, Response] =
    ClientFactory("skills-api", skillsAPIHost, Some(sSLContext))

  lazy val programmersService: APIService[Programmer] = ProgrammersService(programmersClient)
  lazy val skillsService: APIService[Skill]           = SkillsService(skillsClient)

  val api: Service[Request, Response] = (integrationApi :+: skillsApi :+: programmersApi)
    .handle({
      case e: Exception => NotFound(e)
    })
    .toServiceAs[Application.Json]

  val corsFiltered: Service[Request, Response] = corsFilter andThen api

  val filteredAPI: Service[Request, Response] =
    HeaderValidator("auth-token", "UltraSecretLunatechPassword") andThen corsFiltered

  def main(): Unit = {
    logger.info("Serving the IntegrationAPI")
    SqlController.createDatabase

    val server = ServerFactory("MainServer", statsReceiver, port, corsFiltered, None)
    val filteredServer =
      ServerFactory("FilteredServer", statsReceiver, filteredPort, filteredAPI, None)
    val tlsServer =
      ServerFactory("TLSServer", statsReceiver, tlsPort, filteredAPI, Some(sSLContext))

    onExit { server.close(); filteredServer.close(); tlsServer.close() }

    Await.ready(adminHttpServer)
  }

}
