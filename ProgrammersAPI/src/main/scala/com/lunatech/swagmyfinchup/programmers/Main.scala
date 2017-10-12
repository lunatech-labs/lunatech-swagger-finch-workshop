package com.lunatech.swagmyfinchup.programmers

import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import com.lunatech.swagmyfinchup.programmers.controllers.SqlController
import com.lunatech.swagmyfinchup.programmers.utils.ServerFactory
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
  val tlsPort: Flag[Int] = flag("port", 38080, "TCP port for HTTP server")

  val programmersCounter: Counter = statsReceiver.counter("programmers")

  override def defaultHttpPort: Int = 9081

  val sSLContext: SSLContext = {
    val keystore = getClass.getClassLoader.getResourceAsStream("keystore.jks")
    val truststore = getClass.getClassLoader.getResourceAsStream("truststore.jks")
    val passphrase      = "17555599"
    createTlsContext(keystore, passphrase, passphrase, truststore, passphrase)
  }

  def createTlsContext(keyStore: InputStream, keyStorePassphrase: String, privateKeyPassphrase: String,
                       trustKeystore                    : InputStream, trustKeystorePassphrase: String): SSLContext = {

    require(Option(keyStore).isDefined, "Client keystore must be defined")
    require(Option(trustKeystore).isDefined, "Trust store must be defined")

    // Create and initialize the SSLContext with key material
    val clientKeystorePassphraseChars = keyStorePassphrase.toCharArray
    val clientKeyPassphraseChars = privateKeyPassphrase.toCharArray
    val trustKeystorePassphraseChars = trustKeystorePassphrase.toCharArray

    // First initialize the key and trust material
    val ksKeys = KeyStore.getInstance("JKS")
    ksKeys.load(keyStore, clientKeystorePassphraseChars)
    val ksTrust = KeyStore.getInstance("JKS")
    ksTrust.load(trustKeystore, trustKeystorePassphraseChars)

    // KeyManagers decide which key material to use
    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    kmf.init(ksKeys, clientKeyPassphraseChars)

    // TrustManagers decide whether to allow connections
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
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

    val server: ListeningServer = ServerFactory("ProgrammersAPI", statsReceiver, port, api, None)
    val tlsServer: ListeningServer =
      ServerFactory("TLSServer", statsReceiver, tlsPort, api, Some(sSLContext))

    onExit { server.close(); tlsServer.close() }

    Await.ready(adminHttpServer)
  }

}
