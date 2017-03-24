package com.lunatech.swagmyfinchup.programmers.views

import io.finch._
import com.twitter.finagle.http.Status
import com.twitter.logging._
import org.scalatest.{FlatSpec, Matchers}
import com.twitter.finagle.stats._
import com.lunatech.swagmyfinchup.programmers.controllers.SqlController

class ProgrammersAPITest extends FlatSpec with Matchers with ProgrammersAPI {

  val log = Logger.get(getClass)

  private[this] val rootReceiver = new MetricsStatsReceiver()
  val sr                         = new RollupStatsReceiver(rootReceiver)

  val programmersCounter: com.twitter.finagle.stats.Counter = sr.counter("programmers-test")

  behavior of "programmers endpoints"

  SqlController.createDatabase

  it should "have return list programmers of size 11" in {
    val result = listProgrammers(Input.get("/programmers"))

    assert(result.awaitOutputUnsafe().map(_.status) == Some(Status.Ok))

    assert(result.awaitValueUnsafe().get.size == 11)

  }

}
