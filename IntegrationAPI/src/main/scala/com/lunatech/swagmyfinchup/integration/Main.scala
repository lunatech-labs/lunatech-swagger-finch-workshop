package com.lunatech.swagmyfinchup.integration

import com.lunatech.swagmyfinchup.integration.controllers.impl.{ProgrammersService, SkillsService}
import com.lunatech.swagmyfinchup.integration.controllers.{APIService, SqlController}
import com.lunatech.swagmyfinchup.integration.filters.{CORSFilter, HeaderValidator}
import com.lunatech.swagmyfinchup.integration.models.{Programmer, Skill}
import com.lunatech.swagmyfinchup.integration.utils.Helpers.CorsConfig
import com.lunatech.swagmyfinchup.integration.views.{IntegrationAPI, ProgrammersAPI, SkillsAPI}
import com.twitter.app.Flag
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Resolver, Service}
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

import scala.concurrent.ExecutionContext.Implicits.global

object Main
    extends TwitterServer
    with IntegrationAPI
    with ProgrammersAPI
    with SkillsAPI
    with CORSFilter {

  val conf = ConfigFactory.load()

  val port: Flag[Int]         = flag("port", 8090, "TCP port for HTTP server")
  val filteredPort: Flag[Int] = flag("port", 8095, "TCP port for HTTP server")

  override def defaultHttpPort: Int = 9985

  val derivedParser: ConfigParser[CorsConfig] = ConfigParser.derived[CorsConfig]
  val corsconfig: CorsConfig                  = validateConfig(derivedParser.under("server.cors").parse(conf))
  val corsFilter: HttpFilter                  = new Cors.HttpFilter(corspolicy(corsconfig))

  lazy val programmersService: APIService[Programmer] = ProgrammersService(
    Resolver.eval(conf.getString("services.programmers.host")))

  lazy val skillsService: APIService[Skill] = SkillsService(
    Resolver.eval(conf.getString("services.skills.host")))

  val api: Service[Request, Response] = (integrationApi :+: skillsApi :+: programmersApi)
    .handle({
      case e: Exception => NotFound(e)
    })
    .toServiceAs[Application.Json]

  val corsFiltered: Service[Request, Response] = corsFilter andThen api

  val filteredAPI: Service[Request, Response] =
    HeaderValidator("auth-token", "UltraSecretLunatechPassword") andThen corsFiltered

  def main(): Unit = {
    log.info("Serving the IntegrationAPI")
    SqlController.createDatabase

    val server         = Http.server.serve(s":${port()}", corsFiltered)
    val filteredServer = Http.server.serve(s":${filteredPort()}", filteredAPI)

    onExit { server.close(); filteredServer.close() }

    Await.ready(adminHttpServer)
  }

}
