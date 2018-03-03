package com.lunatech.swagmyfinchup.integration.utils

import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object TLSFactory {

  def createTlsContext(keystoreName: String,
                       truststoreName: String,
                       passphrase: String): SSLContext = {
    val keystore   = getClass.getClassLoader.getResourceAsStream(keystoreName)
    val truststore = getClass.getClassLoader.getResourceAsStream(truststoreName)
    createTlsContext(keystore, passphrase, passphrase, truststore, passphrase)
  }

  private def createTlsContext(keyStore: InputStream,
                               keyStorePassphrase: String,
                               privateKeyPassphrase: String,
                               trustKeystore: InputStream,
                               trustKeystorePassphrase: String): SSLContext = {

    require(Option(keyStore).isDefined, "Client keystore must be defined")
    require(Option(trustKeystore).isDefined, "Trust store must be defined")

    // Create and initialize the SSLContext with key material
    val clientKeystorePassphraseChars = keyStorePassphrase.toCharArray
    val clientKeyPassphraseChars      = privateKeyPassphrase.toCharArray
    val trustKeystorePassphraseChars  = trustKeystorePassphrase.toCharArray

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

}
