package com.lunatech.swagmyfinchup.integration.views

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.controllers.{
  APIService,
  CombinerController,
  SqlController
}
import com.lunatech.swagmyfinchup.integration.models._
import com.lunatech.swagmyfinchup.integration.views.Routes._
import com.twitter.logging.Logger
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

trait IntegrationAPI {

  val log: Logger
  def programmersService: APIService[Programmer]
  def skillsService: APIService[Skill]

  val postedIntegration: Endpoint[Integration] =
    jsonBody[UUID => Integration].map(_(UUID.randomUUID()))
  def insertIntegration: Endpoint[Integrated] =
    post(integrations :: postedIntegration) { (integration: Integration) =>
      CombinerController createIntegration (integration, programmersService, skillsService) map {
        case Right(integration) => Created(integration)
        case Left(e) => {
          log.error("Boooom", e)
          InternalServerError(e)
        }
      }
    }

  def getIntegration: Endpoint[Integrated] =
    get(integrations :: uuid) { id: UUID =>
      CombinerController readIntegration (id, programmersService, skillsService) map {
        case Right(u) => Ok(u)
        case Left(e) => {
//          log.error("Boooom", e)
          println(e)
          InternalServerError(e)
        }
      }
    }

  def patchedIntegration: Endpoint[Integration => Integration] =
    jsonBody[Integration => Integration]
  def updateIntegration: Endpoint[Integrated] =
    patch(integrations :: uuid :: patchedIntegration) {
      (id: UUID, integrationToIntegration: Integration => Integration) =>
        CombinerController updateIntegration (id, integrationToIntegration, programmersService, skillsService) map {
          case Right(integration) => Created(integration)
          case Left(e) => {
            log.error("Boooom", e)
            InternalServerError(e)
          }
        }
    }

  def deleteIntegration: Endpoint[Int] =
    delete(integrations :: uuid) { id: UUID =>
      SqlController deleteIntegration id map {
        case Right(u) => Ok(u)
        case Left(e) => {
          log.error("Boooom", e)
          InternalServerError(e)
        }
      }
    }

  def listIntegrations: Endpoint[Seq[Integration]] =
    get(integrations :: skip :: limit) { (skp: Int, lm: Int) =>
      SqlController listIntegrations (skp, lm) map {
        case Right(u) => Ok(u)
        case Left(e) => {
          log.error("Boooom", e)
          InternalServerError(e)
        }
      }
    }

  def listSkillsForProgrammer: Endpoint[Seq[IntegratedSkill]] =
    get(integrations :: skills :: uuid :: skip :: limit) { (id: UUID, skp: Int, lm: Int) =>
      CombinerController readIntegrationsForProgrammer (id, skp, lm, skillsService) map {
        case Right(integration) => Ok(integration)
        case Left(e) => {
          log.error("Boooom", e)
          InternalServerError(e)
        }
      }
    }

  def listProgrammersForSkill: Endpoint[Seq[IntegratedProgrammer]] =
    get(integrations :: programmers :: uuid :: skip :: limit) { (id: UUID, skp: Int, lm: Int) =>
      CombinerController readIntegrationsForSkill (id, skp, lm, programmersService) map {
        case Right(integration) => Ok(integration)
        case Left(e) => {
          log.error("Boooom", e)
          InternalServerError(e)
        }
      }
    }

  val integrationApi = getIntegration :+: deleteIntegration :+: listIntegrations :+: insertIntegration :+: updateIntegration :+: listSkillsForProgrammer :+: listProgrammersForSkill

}
