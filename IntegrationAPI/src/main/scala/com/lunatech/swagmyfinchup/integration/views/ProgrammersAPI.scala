package com.lunatech.swagmyfinchup.integration.views

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.controllers.APIService
import com.lunatech.swagmyfinchup.integration.models.Programmer
import com.lunatech.swagmyfinchup.integration.views.Routes._
import io.circe.Json
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

trait ProgrammersAPI extends Encoders {

  def programmersService: APIService[Programmer]

  val postedProgrammer: Endpoint[Programmer] =
    jsonBody[UUID => Programmer].map(_(UUID.randomUUID()))
  def insertProgrammer: Endpoint[Programmer] =
    post(programmers :: postedProgrammer) { (integration: Programmer) =>
      programmersService create integration map {
        case Right(programmer) => Created(programmer)
        case Left(e)           => InternalServerError(e)
      }
    }

  def getProgrammer: Endpoint[Programmer] =
    get(programmers :: uuid) { id: UUID =>
      programmersService read id map {
        case Right(programmer) => Ok(programmer)
        case Left(e)           => InternalServerError(e)
      }
    }

  def patchedProgrammer: Endpoint[Programmer => Programmer] =
    jsonBody[Programmer => Programmer]
  def updateProgrammer: Endpoint[Programmer] =
    patch(programmers :: uuid :: patchedProgrammer) {
      (id: UUID, integrationProgrammeroProgrammer: Programmer => Programmer) =>
        programmersService update (id, integrationProgrammeroProgrammer) map {
          case Right(programmer) => Created(programmer)
          case Left(e)           => InternalServerError(e)
        }
    }

  def deleteProgrammer: Endpoint[Int] =
    delete(programmers :: uuid) { id: UUID =>
      programmersService delete id map {
        case Right(programmer) => Ok(programmer)
        case Left(e)           => InternalServerError(e)
      }
    }

  def listProgrammers: Endpoint[Seq[Programmer]] =
    get(programmers :: skip :: limit) { (skp: Int, lm: Int) =>
      programmersService list (skp, lm) map {
        case Right(programmer) => Ok(programmer)
        case Left(e)           => InternalServerError(e)
      }
    }

  def programmersDocs: Endpoint[Json] =
    get(programmers :: docs) {
      programmersService docs () map {
        case Right(doc) => Ok(doc)
        case Left(e)    => InternalServerError(e)
      }
    }

  def getSlow: Endpoint[Programmer] =
    get(programmers :: slow) {
      programmersService readSlow () map {
        case Right(programmer) => Ok(programmer)
        case Left(e)           => InternalServerError(e)
      }
    }

  val programmersApi = getProgrammer :+: deleteProgrammer :+: listProgrammers :+: insertProgrammer :+: updateProgrammer :+: programmersDocs :+: getSlow

}
