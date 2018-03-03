package com.lunatech.swagmyfinchup.integration.utils

import javax.net.ssl.SSLContext

import com.twitter.app.Flag
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.finagle.{Http, Service}

object ServerFactory {

  def apply(label: String,
            statsReceiver: StatsReceiver,
            port: Flag[Int],
            api: Service[Request, Response],
            sslContextOpt: Option[SSLContext]) = {
    Http.server
      .withLabel(label)
      .withStatsReceiver(statsReceiver)
      .withTls(sslContextOpt)
      .configured(Http.Netty4Impl)
      .serve(s":${port()}", api)
  }

  implicit class ServerOps(server: Http.Server) {

    def withTls(sslContextOpt: Option[SSLContext]) =
      sslContextOpt
        .map { sslContex =>
          server.withTransport.tls(sslContex)
        }
        .getOrElse(server)

  }

}
