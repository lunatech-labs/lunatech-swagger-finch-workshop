package com.lunatech.swagmyfinchup.programmers.views

import io.finch._
import io.finch.Endpoint._
import shapeless.HNil

object Routes {

  val programmers: Endpoint[HNil] = "programmers"
  val batch: Endpoint[HNil]       = "batch"
  val docs: Endpoint[HNil]        = "docs"
  val slow: Endpoint[HNil]        = "slow"

  val uid: Endpoint[Option[String]] = paramOption("userId")
  val q: Endpoint[Option[String]]   = paramOption("searchString")
  val skip: Endpoint[Int]           = paramOption[Int]("skip").withDefault(0)
  val limit: Endpoint[Int]          = paramOption[Int]("limit").withDefault(100)

}
