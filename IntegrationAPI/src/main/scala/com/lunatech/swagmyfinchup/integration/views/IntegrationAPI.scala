package com.lunatech.swagmyfinchup.integration.views

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.controllers.SqlController
import com.lunatech.swagmyfinchup.integration.models._
import com.lunatech.swagmyfinchup.integration.views.Routes._
import com.twitter.logging.Logger
import io.finch._

trait IntegrationAPI {

  val log: Logger

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


  val integrationApi = deleteIntegration :+: listIntegrations

}
