package com.lunatech.swagmyfinchup.programmers.views

import java.util.UUID

import com.lunatech.swagmyfinchup.programmers.controllers.SqlController
import com.lunatech.swagmyfinchup.programmers.models._
import com.lunatech.swagmyfinchup.programmers.views.Routes._
import com.twitter.finagle.stats.Counter
import com.twitter.logging.Logger
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

trait ProgrammersAPI extends Encoders {

  val log: Logger
  val programmersCounter: Counter

  val postedProgrammer: Endpoint[Programmer] =
    jsonBody[UUID => Programmer].map(_(UUID.randomUUID()))
  def insertProgrammer: Endpoint[Programmer] =
    post(programmers :: postedProgrammer) { (programmer: Programmer) =>
      SqlController insertProgrammer programmer map {
        case Right(programmer) => {
          programmersCounter.incr()
          log.info(s"Created Programmer ${programmer.id}")
          Created(programmer)
        }
        case Left(e) => solveException(e)
      }
    }

  def getProgrammer: Endpoint[Programmer] =
    get(programmers :: uuid) { id: UUID =>
      SqlController getProgrammer id map {
        case Right(u) =>
          log.info(s"Found Programmer with email ${u.email}")
          Ok(u)
        case Left(e) => solveException(e)
      }
    }

//  def patchedProgrammer: Endpoint[Programmer => Programmer] = jsonBody[Programmer => Programmer]
//  def updateProgrammer: Endpoint[Programmer]                = //???

//  def deleteProgrammer: Endpoint[Int] = //???

  def listProgrammers: Endpoint[Seq[Programmer]] =
    get(programmers :: skip :: limit) { (skp: Int, lm: Int) =>
      SqlController listProgrammers (skp, lm) map {
        case Right(u) =>
          log.info(s"Found List of Programmer with size ${u.size}")
          Ok(u)
        case Left(e) => solveException(e)
      }
    }

  val programmersApi = getProgrammer :+: listProgrammers :+: insertProgrammer

  def solveException(e: SMFUEerror): Output[Nothing] = {
    e.httpCode match {
      case 404 => NotFound(e)
      case _   => InternalServerError(e)
    }
  }

}
