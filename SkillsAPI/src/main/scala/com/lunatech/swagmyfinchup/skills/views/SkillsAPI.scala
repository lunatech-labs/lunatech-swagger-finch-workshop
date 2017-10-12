package com.lunatech.swagmyfinchup.skills.views

import java.util.UUID

import com.lunatech.swagmyfinchup.skills.controllers.SqlController
import com.lunatech.swagmyfinchup.skills.models.{SMFUEerror, Skill}
import com.lunatech.swagmyfinchup.skills.views.Routes._
import com.twitter.finagle.http.Response
import com.twitter.finagle.stats.Counter
import com.twitter.io.Buf
import com.twitter.logging.Logger
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

trait SkillsAPI extends Encoders {

  val log: Logger
  val skillsCounter: Counter

  def swaggerdocs: Endpoint[Response] =
    get(skills :: docs) {
      val swagger =
        """{"swagger":"2.0","info":{"title":"Skills API","description":"A tool to get data about skills","version":"1.0.0"},"host":"swag-my-finch-up.lunatech.com","schemes":["https"],"basePath":"/v1","produces":["application/json"],"paths":{"/skill":{"post":{"summary":"creates a Skill","description":"This endpoint allows the creation of a Skill defined as a json in the body of this request","parameters":[{"name":"Skill","in":"body","required":true,"schema":{"$ref":"#/definitions/Skill"}}],"tags":["Create","Skills"],"responses":{"202":{"description":"A newly created Skill","schema":{"$ref":"#/definitions/Skill"}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}}},"/skill/{id}":{"get":{"summary":"Reads a Skill","description":"The Skill endpoint returns a Skill specified by the id parameter.\n","parameters":[{"name":"id","in":"path","description":"Id of the user being requested.","required":true,"type":"string"}],"tags":["Read","Skills"],"responses":{"200":{"description":"A Skill specified by id","schema":{"$ref":"#/definitions/Skill"}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}},"put":{"summary":"Updates a Skill","description":"The Skill endpoint returns a Skill specified by the id parameter.\n","parameters":[{"name":"id","in":"path","description":"Id of the user being updated.","required":true,"type":"string"},{"name":"Skill","in":"body","required":true,"schema":{"$ref":"#/definitions/Skill"}}],"tags":["Update","Skills"],"responses":{"200":{"description":"The data of the updated Skill","schema":{"$ref":"#/definitions/Skill"}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}},"delete":{"summary":"Deletes a Skill","description":"some","parameters":[{"name":"id","in":"path","description":"Id of the user being deleted.","required":true,"type":"string"}],"tags":["Delete","Skills"],"responses":{"200":{"description":"The data of the deleted Skill","schema":{"$ref":"#/definitions/Skill"}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}}},"/skills":{"get":{"summary":"Lists Skills","description":"The Skills endpoint returns a list of Skills available.\n","parameters":[{"name":"start","in":"query","description":"Reference to the previous page requested.","required":false,"type":"number","format":"integer","minimum":0,"default":0},{"name":"limit","in":"query","description":"Size of the page requested.","required":false,"type":"number","format":"integer","minimum":1,"default":100},{"name":"skill","in":"query","description":"array of Id of skills to be used as filter","required":false,"type":"array","items":{"type":"string"}}],"tags":["List","Skills"],"responses":{"200":{"description":"An array of Skills","schema":{"type":"array","items":{"$ref":"#/definitions/Skill"}}},"default":{"description":"Unexpected error","schema":{"$ref":"#/definitions/Error"}}}}}},"definitions":{"Skill":{"type":"object","properties":{"id":{"type":"string","description":"Unique identifier representing a specific Lunatech Skill. For example, Scala will have a different id than Java."},"name":{"type":"string","description":"Name of the Skill."},"domain":{"type":"string","description":"field in which this skill works. For Example, programming, management, etc"}}},"Error":{"type":"object","properties":{"code":{"type":"integer","format":"int32"},"message":{"type":"string"},"fields":{"type":"string"}}}}}"""
      val rep = Response()
      rep.content = Buf.Utf8(swagger)
      rep.contentType = "application/json"
      rep
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
      case e: Exception        => InternalServerError(e)
    }

  val postedSkill: Endpoint[Skill] =
    jsonBody[UUID => Skill].map(_(UUID.randomUUID()))
  def insertSkill: Endpoint[Skill] =
    post(skills :: postedSkill) { (skill: Skill) =>
      SqlController insertSkill skill map {
        case Right(skill) => {
          skillsCounter.incr()
          Created(skill)
        }
        case Left(e) => solveException(e)
      }
    }

  def getSkill: Endpoint[Skill] =
    get(skills :: uuid) { id: UUID =>
      SqlController getSkill id map {
        case Right(u) => Ok(u)
        case Left(e)  => solveException(e)
      }
    }

  def patchedSkill: Endpoint[Skill => Skill] = jsonBody[Skill => Skill]
  def updateSkill: Endpoint[Skill] =
    patch(skills :: uuid :: patchedSkill) { (id: UUID, skillToSkill: Skill => Skill) =>
      SqlController updateSkill (id, skillToSkill) map {
        case Right(skill) => Created(skill)
        case Left(e)      => solveException(e)
      }
    }

  def deleteSkill: Endpoint[Int] =
    delete(skills :: uuid) { id: UUID =>
      SqlController deleteSkill id map {
        case Right(u) => Ok(u)
        case Left(e)  => solveException(e)
      }
    }

  def listSkills: Endpoint[Seq[Skill]] =
    get(skills :: skip :: limit) { (skp: Int, lm: Int) =>
      SqlController listSkills (skp, lm) map {
        case Right(u) => Ok(u)
        case Left(e)  => solveException(e)
      }
    }

  val skillsApi = swaggerdocs :+: getSkill :+: deleteSkill :+: listSkills :+: insertSkill :+: updateSkill

  def solveException(e: SMFUEerror): Output[Nothing] = {
    e.httpCode match {
      case 404 => NotFound(e)
      case _   => InternalServerError(e)
    }
  }

}
