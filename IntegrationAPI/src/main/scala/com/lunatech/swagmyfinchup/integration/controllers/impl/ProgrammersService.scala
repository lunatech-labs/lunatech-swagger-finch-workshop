package com.lunatech.swagmyfinchup.integration.controllers.impl

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.controllers.APIService
import com.lunatech.swagmyfinchup.integration.models.{Programmer, _}
import com.twitter.conversions.time._
import com.twitter.finagle.http.param.Streaming
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.netty3.ChannelBufferBuf
import com.twitter.finagle.param.HighResTimer
import com.twitter.finagle.service.{Backoff, RetryBudget, RetryFilter}
import com.twitter.finagle.{Http, Name, Service}
import com.twitter.util.{Future, Return, Try}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.util.CharsetUtil._

import scala.concurrent.ExecutionContext

class ProgrammersService(override val client: Service[Request, Response],
                         override val apiName: String)(implicit val ec: ExecutionContext)
    extends APIService[Programmer] {

  private def processProgrammer(json: Json): Either[SMFUEerror, Programmer] =
    json.as[Programmer] match {
      case Left(e)       => Left(ParseException("programmer", e.getMessage))
      case Right(result) => Right(result)
    }

  private def processProgrammers(json: Json): Either[SMFUEerror, Seq[Programmer]] =
    json.as[Seq[Programmer]] match {
      case Left(e)       => Left(ParseException("programmer", e.getMessage))
      case Right(result) => Right(result)
    }

  override def processInt(json: Json): Either[SMFUEerror, Int] =
    json.as[Int] match {
      case Left(e)       => Left(CreationException("generic", e.getMessage))
      case Right(result) => Right(result)
    }

  override def create(registry: Programmer): Future[Either[SMFUEerror, Programmer]] =
    Future.value(Right(Programmer(UUID.randomUUID(), "", "", "")))

  override def read(id: UUID): Future[Either[SMFUEerror, Programmer]] =
    Future.value(Right(Programmer(UUID.randomUUID(), "", "", "")))

  override def update(
      id: UUID,
      registryF: (Programmer) => Programmer): Future[Either[SMFUEerror, Programmer]] =
    Future.value(Right(Programmer(UUID.randomUUID(), "", "", "")))

  override def delete(id: UUID): Future[Either[SMFUEerror, Int]] = Future.value(Right(1))

  override def list(skip: Int, limit: Int): Future[Either[SMFUEerror, Seq[Programmer]]] =
    Future.value(Right(Seq.empty[Programmer]))

  override def docs(): Future[Either[SMFUEerror, Json]] =
    Future.value(Right(Json.fromString("{}")))

  override def readSlow(): Future[Either[SMFUEerror, Programmer]] =
    Future.value(Right(Programmer(UUID.randomUUID(), "", "", "")))
}

object ProgrammersService {

  def apply(instances: Name)(implicit ec: ExecutionContext): ProgrammersService = {
    val service: Service[Request, Response] = Http.client.newService(instances, "programmers-api")
    new ProgrammersService(service, "programmers-api")
  }

}
