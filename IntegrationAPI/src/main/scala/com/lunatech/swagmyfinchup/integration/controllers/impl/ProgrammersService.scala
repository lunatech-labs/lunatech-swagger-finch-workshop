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

  override def create(registry: Programmer): Future[Either[SMFUEerror, Programmer]] = {
    val request: Request = Request(Method.Post, "/programmers")
    request.content = ChannelBufferBuf.newOwned(copiedBuffer(registry.asJson.toString(), UTF_8))
    client(request) map (resp => {
                           if (resp.getStatusCode() >= 400)
                             Left(CreationException(
                               "Programmer",
                               s"$apiName error: ${resp.getStatusCode} - ${resp.getContentString}"))
                           else {
                             parse(resp.contentString) match {
                               case Left(e)     => Left(CreationException("Programmer", e.getMessage))
                               case Right(json) => processProgrammer(json)
                             }
                           }
                         })
  }

  override def read(id: UUID): Future[Either[SMFUEerror, Programmer]] = {
    val request: Request = Request(Method.Get, s"/programmers/$id")
    client(request) map (resp => {
                           if (resp.getStatusCode() >= 400)
                             Left(CreationException(
                               "Programmer",
                               s"$apiName error: ${resp.getStatusCode} - ${resp.getContentString}"))
                           else {
                             parse(resp.contentString) match {
                               case Left(e)     => Left(CreationException("Programmer", e.getMessage))
                               case Right(json) => processProgrammer(json)
                             }
                           }
                         })
  }

  override def update(
      id: UUID,
      registryF: Programmer => Programmer): Future[Either[SMFUEerror, Programmer]] = {
    val patchedProgrammer: Programmer = registryF(Programmer(id, "dummy", "dummy", "dummy"))
    val request: Request =
      Request(Method.Patch, s"/programmers/$id")
    request.content =
      ChannelBufferBuf.newOwned(copiedBuffer(patchedProgrammer.asJson.toString(), UTF_8))
    client(request) map (resp => {
                           if (resp.getStatusCode() >= 400)
                             Left(CreationException(
                               "Programmer",
                               s"$apiName error: ${resp.getStatusCode} - ${resp.getContentString}"))
                           else {
                             parse(resp.contentString) match {
                               case Left(e)     => Left(CreationException("Programmer", e.getMessage))
                               case Right(json) => processProgrammer(json)
                             }
                           }
                         })
  }

  override def delete(id: UUID): Future[Either[SMFUEerror, Int]] = {
    val request: Request = Request(Method.Delete, s"/programmers/$id")
    client(request) map (resp => {
                           if (resp.getStatusCode() >= 400)
                             Left(CreationException(
                               "Programmer",
                               s"$apiName error: ${resp.getStatusCode} - ${resp.getContentString}"))
                           else {
                             parse(resp.contentString) match {
                               case Left(e)     => Left(CreationException("Programmer", e.getMessage))
                               case Right(json) => processInt(json)
                             }
                           }
                         })
  }

  override def list(skip: Int, limit: Int): Future[Either[SMFUEerror, Seq[Programmer]]] = {
    val request: Request = Request(Method.Get, "/programmers")
    client(request) map (resp => {
                           if (resp.getStatusCode() >= 400)
                             Left(CreationException(
                               "Programmer",
                               s"$apiName error: ${resp.getStatusCode} - ${resp.getContentString}"))
                           else {
                             parse(resp.contentString) match {
                               case Left(e)     => Left(CreationException("Programmer", e.getMessage))
                               case Right(json) => processProgrammers(json)
                             }
                           }
                         })
  }

  override def docs(): Future[Either[SMFUEerror, Json]] = {
    val request: Request = Request(Method.Get, "/programmers/docs")
    client(request) map (resp => {
                           if (resp.getStatusCode() >= 400)
                             Left(CreationException(
                               "Programmer",
                               s"$apiName error: ${resp.getStatusCode} - ${resp.getContentString}"))
                           else {
                             parse(resp.contentString) match {
                               case Left(e)     => Left(CreationException("Programmer", e.getMessage))
                               case Right(json) => Right(json)
                             }
                           }
                         })
  }

  override def readSlow(): Future[Either[SMFUEerror, Programmer]] = {
    val request: Request = Request(Method.Get, "/programmers/slow")
    client(request) map (resp => {
                           if (resp.getStatusCode() >= 400)
                             Left(CreationException(
                               "Programmer",
                               s"$apiName error: ${resp.getStatusCode} - ${resp.getContentString}"))
                           else {
                             parse(resp.contentString) match {
                               case Left(e)     => Left(CreationException("Programmer", e.getMessage))
                               case Right(json) => processProgrammer(json)
                             }
                           }
                         })
  }

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

}

object ProgrammersService {

  def apply(instances: Name)(implicit ec: ExecutionContext): ProgrammersService = {

    val shouldRetry: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Return(rep)) => rep.status != 200
    }

    implicit val t                          = HighResTimer.Default
    val retry                               = RetryFilter(Backoff.const(1.second).take(3))(shouldRetry)
    val service: Service[Request, Response] = Http.client.newService(instances, "programmers-api")
    new ProgrammersService(retry.andThen(service), "programmers-api")
  }

}
