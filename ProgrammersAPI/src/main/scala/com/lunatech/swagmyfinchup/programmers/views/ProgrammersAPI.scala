package com.lunatech.swagmyfinchup.programmers.views

import java.util.UUID

import com.lunatech.swagmyfinchup.programmers.controllers.SqlController
import com.lunatech.swagmyfinchup.programmers.models._
import com.lunatech.swagmyfinchup.programmers.views.Routes._
import com.twitter.finagle.http.Response
import com.twitter.finagle.stats.Counter
import com.twitter.io.Buf
import com.twitter.util.logging.Logging
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._

import scala.util.Random

trait ProgrammersAPI extends Encoders with Logging {

//  val logger: Logger
  val programmersCounter: Counter

  def swaggerdocs: Endpoint[Response] =
    get(programmers :: docs) {
      val swagger =
        """{"swagger":"2.0","info":{"description":"This is the API to give you a list of Programmers","version":"1.0","title":"Programmers API","contact":{"email":"you@domain.com"},"license":{"name":"Apache 2.0","url":"http://www.apache.org/lincenses/LICENSE-2.0.html"}},"host":"localhost","basePath":"/v1","schemes":["http"],"consumes":["application/json"],"produces":["application/json"],"paths":{"/programmers":{"get":{"summary":"Lists programmers","description":"The programmers endpoint returns a list of programmers available.\n","parameters":[{"name":"skip","in":"query","description":"Reference to the previous page requested.","required":false,"type":"number","format":"integer","minimum":0,"default":0},{"$ref":"#/parameters/limit"}],"tags":["List","Programmers"],"responses":{"200":{"description":"An array of programmers","schema":{"type":"array","items":{"$ref":"#/definitions/Programmer"}}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}},"post":{"summary":"creates a Programmer","description":"This endpoint allows the creation of a programmer defined as a json in the body of this request","parameters":[{"name":"programmer","in":"body","required":true,"schema":{"$ref":"#/definitions/Programmer"}}],"tags":["Create","Programmers"],"responses":{"201":{"description":"A newly created programmer","schema":{"$ref":"#/definitions/Programmer"}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}}},"/programmers/{pid}":{"get":{"summary":"Reads a programmer","description":"The programmer endpoint returns a programmer specified by the id parameter.\n","parameters":[{"$ref":"#/parameters/pid"}],"tags":["Read","Programmers"],"responses":{"200":{"description":"A programmer specified by id","schema":{"$ref":"#/definitions/Programmer"}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}}}},"parameters":{"pid":{"name":"pid","in":"path","description":"Id of the user being requested.","required":true,"type":"string","format":"uuid"},"queryString":{"in":"query","name":"q","description":"querystring","type":"string","format":"string"},"start":{"in":"query","name":"start","description":"start of pagination","type":"integer","format":"int32","minimum":0,"maximum":50,"default":0},"limit":{"in":"query","name":"limit","description":"Maximum number of records to return from start","type":"integer","format":"int32","minimum":0,"maximum":50}},"responses":{"BadRequest":{"description":"Invalid input","schema":{"$ref":"#/definitions/Error"}},"Unauthorized":{"description":"You are not authenticated into the system","schema":{"$ref":"#/definitions/Error"}},"InternalServerError":{"description":"An unexpected error occured","schema":{"$ref":"#/definitions/Error"}}},"definitions":{"Programmer":{"type":"object","properties":{"id":{"$ref":"#/definitions/pid"},"name":{"type":"string","description":"Name of the programmer."},"lastName":{"type":"string","description":"Last name of the programmer."},"email":{"type":"string","format":"email","description":"programmer's email"}}},"Error":{"type":"object","properties":{"code":{"type":"integer","format":"int32"},"message":{"type":"string"},"cause":{"type":"string"}}},"pid":{"type":"string","format":"uuid"}}}"""
      val rep = Response()
      rep.content = Buf.Utf8(swagger)
      rep.contentType = "application/json"
      rep
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
      case e: Exception        => InternalServerError(e)
    }

  val postedProgrammer: Endpoint[Programmer] =
    jsonBody[UUID => Programmer].map(_(UUID.randomUUID()))
  def insertProgrammer: Endpoint[Programmer] =
    post(programmers :: postedProgrammer) { (programmer: Programmer) =>
      SqlController insertProgrammer programmer map {
        case Right(programmer) => {
          programmersCounter.incr()
          Created(programmer)
        }
        case Left(e) => solveException(e)
      }
    }

  type NewProgrammer = UUID => Programmer

  val postedProgrammers: Endpoint[Seq[Programmer]] =
    jsonBody[Seq[NewProgrammer]].map((ff: Seq[NewProgrammer]) =>
      ff.map((f: NewProgrammer) => f(UUID.randomUUID())))

  def insertProgrammers: Endpoint[Seq[Programmer]] =
    post(programmers :: batch :: postedProgrammers) { (programmers: Seq[Programmer]) =>
      SqlController insertProgrammers programmers map {
        case Right(u) => Ok(u)
        case Left(e)  => solveException(e)
      }
    }

  def getProgrammer: Endpoint[Programmer] =
    get(programmers :: path[UUID]) { id: UUID =>
      SqlController getProgrammer id map {
        case Right(u) => Ok(u)
        case Left(e)  => solveException(e)
      }
    }

  def patchedProgrammer: Endpoint[Programmer => Programmer] = jsonBody[Programmer => Programmer]
  def updateProgrammer: Endpoint[Programmer] =
    patch(programmers :: path[UUID] :: patchedProgrammer) {
      (id: UUID, programmerToProgrammer: Programmer => Programmer) =>
        SqlController updateProgrammer (id, programmerToProgrammer) map {
          case Right(programmer) => Created(programmer)
          case Left(e)           => solveException(e)
        }
    }

  def deleteProgrammer: Endpoint[Int] =
    delete(programmers :: path[UUID]) { id: UUID =>
      SqlController deleteProgrammer id map {
        case Right(u) => Ok(u)
        case Left(e)  => solveException(e)
      }
    }

  def listProgrammers: Endpoint[Seq[Programmer]] =
    get(programmers :: skip :: limit) { (skp: Int, lm: Int) =>
      {
        logger.info("Getting request")
        SqlController listProgrammers (skp, lm) map {
          case Right(u) => Ok(u)
          case Left(e)  => solveException(e)
        }
      }
    }

  def getProgrammerSlow: Endpoint[Programmer] =
    get(programmers :: slow) {
      if (Random.nextBoolean()) {
        logger.info("SLOOOOOOOOOOOOOOOW")
        Thread.sleep(15000)
      } else logger.info("FAAAAAAAAAAAAST")
      Ok(
        Programmer(UUID.randomUUID(),
                   Random.nextString(5),
                   Random.nextString(5),
                   s"${Random.nextString(5)}@${Random.nextString(5)}.com"))
    }

  val programmersApi = swaggerdocs :+: getProgrammer :+: listProgrammers :+: insertProgrammer :+: updateProgrammer :+: deleteProgrammer :+: insertProgrammers :+: getProgrammerSlow

  def solveException(e: SMFUEerror): Output[Nothing] = {
    e.httpCode match {
      case 404 => NotFound(e)
      case _   => InternalServerError(e)
    }
  }

}
