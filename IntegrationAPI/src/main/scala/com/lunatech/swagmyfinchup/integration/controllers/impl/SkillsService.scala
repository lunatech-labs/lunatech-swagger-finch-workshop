package com.lunatech.swagmyfinchup.integration.controllers.impl

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.controllers.APIService
import com.lunatech.swagmyfinchup.integration.models.{Skill, _}
import com.twitter.finagle.http.param.Streaming
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Http, Name, Service}
import com.twitter.util.Future
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

class SkillsService(override val client: Service[Request, Response], override val apiName: String)(
    implicit val ec: ExecutionContext)
    extends APIService[Skill] {

  override def create(registry: Skill): Future[Either[SMFUEerror, Skill]] = {
    val request: Request = buildRequest(Method.Post, "/skills", Some(registry.asJson))
    processRequest(request, processSkill)
  }

  override def read(id: UUID): Future[Either[SMFUEerror, Skill]] = {
    val request: Request = buildRequest(Method.Get, s"/skills/$id", None)
    processRequest(request, processSkill)
  }

  override def update(id: UUID, registryF: Skill => Skill): Future[Either[SMFUEerror, Skill]] = {
    val patchedSkill: Skill = registryF(Skill(id, "dummy", "dummy"))
    val request: Request =
      buildRequest(Method.Patch, s"/skills/$id", Some(patchedSkill.asJson))
    processRequest(request, processSkill)
  }

  override def delete(id: UUID): Future[Either[SMFUEerror, Int]] = {
    val request: Request = buildRequest(Method.Delete, s"/skills/$id", None)
    processRequest(request, processInt)
  }

  override def list(skip: Int, limit: Int): Future[Either[SMFUEerror, Seq[Skill]]] = {
    val request: Request = buildRequest(Method.Get, "/skills", None)
    processRequest(request, processSkills)
  }

  override def docs(): Future[Either[SMFUEerror, Json]] = {
    val request: Request = buildRequest(Method.Get, "/skills/docs", None)
    processRequest(request, json => Right(json))
  }

  override def readSlow(): Future[Either[SMFUEerror, Skill]] =
    Future.value(Right(Skill(UUID.randomUUID(), "", "")))

  private def processSkill(json: Json): Either[SMFUEerror, Skill] =
    json.as[Skill] match {
      case Left(e)       => Left(ParseException("skill", e.getMessage))
      case Right(result) => Right(result)
    }

  private def processSkills(json: Json): Either[SMFUEerror, Seq[Skill]] =
    json.as[Seq[Skill]] match {
      case Left(e)       => Left(ParseException("skill", e.getMessage))
      case Right(result) => Right(result)
    }

}

object SkillsService {

  def apply(instances: Name)(implicit ec: ExecutionContext): SkillsService = {
    val service: Service[Request, Response] = Http.client
      .configured(Streaming(enabled = true))
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .newService(instances, "skills-api")
    new SkillsService(service, "skills-api")
  }

}
