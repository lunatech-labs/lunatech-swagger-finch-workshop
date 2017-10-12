package com.lunatech.swagmyfinchup.skills

import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import com.lunatech.swagmyfinchup.skills.controllers.SqlController
import com.lunatech.swagmyfinchup.skills.utils.ServerFactory
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
  val tlsPort: Flag[Int] = flag("port", 38081, "TCP port for HTTP server")

  val skillsCounter: Counter = statsReceiver.counter("skills")

  override def defaultHttpPort: Int = 9995

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

  val api: Service[Request, Response] = skillsApi
    .handle({
      case e: Exception => {
        e.printStackTrace()
        NotFound(e)
      }
    })
    .toServiceAs[Application.Json]

  def main(): Unit = {
    log.info("Serving the SkillsAPI")
    SqlController.createDatabase

    val server    = ServerFactory("SkillsAPI", statsReceiver, port, api, None)
    val tlsServer = ServerFactory("TLSServer", statsReceiver, tlsPort, api, Some(sSLContext))

    onExit { server.close(); tlsServer.close() }

    Await.ready(adminHttpServer)
  }

}
