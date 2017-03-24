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
import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

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
  val tlsPort: Flag[Int]      = flag("port", 38082, "TCP port for HTTP server")

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

  def getSSLContext: SSLContext = {
    // Create and initialize the SSLContext with key material
    val passphrase      = "sample".toCharArray()
    val trustPassphrase = "sample".toCharArray()
    // First initialize the key and trust material
    val ksKeys           = KeyStore.getInstance("JKS")
    val keystoreResource = this.getClass.getClassLoader.getResourceAsStream("sample-keystore.jks")
    ksKeys.load(keystoreResource, passphrase)
    val ksTrust = KeyStore.getInstance("JKS")
    val trustStoreResource =
      this.getClass.getClassLoader.getResourceAsStream("sample-keystore.jks")
    ksTrust.load(trustStoreResource, trustPassphrase)
    // KeyManagers decide which key material to us
    val kmf = KeyManagerFactory.getInstance("SunX509")
    kmf.init(ksKeys, passphrase)
    // TrustManagers decide whether to allow connections
    val tmf = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ksTrust)
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(kmf.getKeyManagers, tmf.getTrustManagers, null)
    sslContext
  }

  def main(): Unit = {
    log.info("Serving the IntegrationAPI")
    SqlController.createDatabase

    val server = Http.server.withLabel("MainServer").serve(s":${port()}", corsFiltered)
    val filteredServer =
      Http.server.withLabel("FilteredServer").serve(s":${filteredPort()}", filteredAPI)
    val tlsServer = Http.server
      .withLabel("TLSServer")
      .withTransport
      .tls(getSSLContext)
      .configured(Http.Netty4Impl)
      .serve(s":${tlsPort()}", filteredAPI)

    onExit { server.close(); filteredServer.close(); tlsServer.close() }

    Await.ready(adminHttpServer)
  }

}
