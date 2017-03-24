package com.lunatech.swagmyfinchup.programmers

import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

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

  val port: Flag[Int]    = flag("port", 8081, "TCP port for HTTP server")
  val tlsPort: Flag[Int] = flag("port", 38080, "TCP port for HTTP server")

  val programmersCounter: Counter = statsReceiver.counter("programmers")

  override def defaultHttpPort: Int = 9081

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

  val api: Service[Request, Response] = programmersApi
    .handle({
      case e: Exception => NotFound(e)
    })
    .toServiceAs[Application.Json]

  def main(): Unit = {
    log.info("Serving the ProgrammersAPI")
    SqlController.createDatabase

    val server = Http.server
      .withLabel("ProgrammersAPI")
      .withStatsReceiver(statsReceiver)
      .serve(s":${port()}", api)

    val tlsServer = Http.server
      .withLabel("TLSServer")
      .withStatsReceiver(statsReceiver)
      .withTransport
      .tls(getSSLContext)
      .configured(Http.Netty4Impl)
      .serve(s":${tlsPort()}", api)

    onExit { server.close(); tlsServer.close() }

    Await.ready(adminHttpServer)
  }

}
