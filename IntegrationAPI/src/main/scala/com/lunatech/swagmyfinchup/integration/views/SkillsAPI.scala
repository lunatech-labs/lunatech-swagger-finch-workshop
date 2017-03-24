package com.lunatech.swagmyfinchup.integration.views

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.controllers.APIService
import com.lunatech.swagmyfinchup.integration.models.Skill
import com.lunatech.swagmyfinchup.integration.views.Routes._
import io.circe.Json
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

trait SkillsAPI {

  def skillsService: APIService[Skill]

  val postedSkill: Endpoint[Skill] =
    jsonBody[UUID => Skill].map(_(UUID.randomUUID()))
  def insertSkill: Endpoint[Skill] =
    post(skills :: postedSkill) { (integration: Skill) =>
      skillsService create integration map {
        case Right(integration) => Created(integration)
        case Left(e)            => InternalServerError(e)
      }
    }

  def getSkill: Endpoint[Skill] =
    get(skills :: uuid) { id: UUID =>
      skillsService read id map {
        case Right(u) => Ok(u)
        case Left(e) => {
          println(e)
          InternalServerError(e)
        }
      }
    }

  def patchedSkill: Endpoint[Skill => Skill] =
    jsonBody[Skill => Skill]
  def updateSkill: Endpoint[Skill] =
    patch(skills :: uuid :: patchedSkill) { (id: UUID, integrationSkilloSkill: Skill => Skill) =>
      skillsService update (id, integrationSkilloSkill) map {
        case Right(skill) => Created(skill)
        case Left(e)      => InternalServerError(e)
      }
    }

  def deleteSkill: Endpoint[Int] =
    delete(skills :: uuid) { id: UUID =>
      skillsService delete id map {
        case Right(u) => Ok(u)
        case Left(e)  => InternalServerError(e)
      }
    }

  def listSkill: Endpoint[Seq[Skill]] =
    get(skills :: skip :: limit) { (skp: Int, lm: Int) =>
      skillsService list (skp, lm) map {
        case Right(skills) => Ok(skills)
        case Left(e)       => InternalServerError(e)
      }
    }

  def skillsDocs: Endpoint[Json] =
    get(skills :: docs) {
      skillsService docs () map {
        case Right(doc) => Ok(doc)
        case Left(e)    => InternalServerError(e)
      }
    }

  val skillsApi = getSkill :+: deleteSkill :+: listSkill :+: insertSkill :+: updateSkill :+: skillsDocs

}
