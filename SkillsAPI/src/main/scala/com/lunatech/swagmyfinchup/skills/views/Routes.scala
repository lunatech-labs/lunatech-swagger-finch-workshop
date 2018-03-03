package com.lunatech.swagmyfinchup.skills.views

import io.finch.{Endpoint, _}
import shapeless.HNil

object Routes {

  val skills: Endpoint[HNil] = "skills"
  val docs: Endpoint[HNil]   = "docs"

  val uid: Endpoint[Option[String]] = paramOption("userId")
  val q: Endpoint[Option[String]]   = paramOption("searchString")
  val skip: Endpoint[Int]           = paramOption[Int]("skip").withDefault(0)
  val limit: Endpoint[Int]          = paramOption[Int]("limit").withDefault(100)

}
