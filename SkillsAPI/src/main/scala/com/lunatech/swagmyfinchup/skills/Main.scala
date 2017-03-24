package com.lunatech.swagmyfinchup.skills

import com.lunatech.swagmyfinchup.skills.controllers.SqlController
import com.lunatech.swagmyfinchup.skills.views.SkillsAPI
import com.twitter.app.Flag
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

object Main extends TwitterServer with SkillsAPI {

  val port: Flag[Int] = flag("port", 8085, "TCP port for HTTP server")

  val skillsCounter: Counter = statsReceiver.counter("skills")

  override def defaultHttpPort: Int = 9995

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

    val server = Http.server.withStatsReceiver(statsReceiver).serve(s":${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }

}
