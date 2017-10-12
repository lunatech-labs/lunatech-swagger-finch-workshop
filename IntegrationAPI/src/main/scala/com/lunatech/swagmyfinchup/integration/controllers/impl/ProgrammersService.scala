package com.lunatech.swagmyfinchup.integration.controllers.impl

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.controllers.APIService
import com.lunatech.swagmyfinchup.integration.models.{Programmer, _}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.netty3.ChannelBufferBuf
import com.twitter.util.Future
import io.circe.Json
import io.circe.generic.auto._
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
    processRequest(request, processProgrammer)
  }

  override def read(id: UUID): Future[Either[SMFUEerror, Programmer]] = {
    val request: Request = Request(Method.Get, s"/programmers/$id")
    processRequest(request, processProgrammer)
  }

  override def update(
      id: UUID,
      registryF: Programmer => Programmer): Future[Either[SMFUEerror, Programmer]] = {
    val patchedProgrammer: Programmer = registryF(Programmer(id, "dummy", "dummy", "dummy"))
    val request: Request =
      Request(Method.Patch, s"/programmers/$id")
    request.content =
      ChannelBufferBuf.newOwned(copiedBuffer(patchedProgrammer.asJson.toString(), UTF_8))
    processRequest(request, processProgrammer)
  }

  override def delete(id: UUID): Future[Either[SMFUEerror, Int]] = {
    val request: Request = Request(Method.Delete, s"/programmers/$id")
    processRequest(request, processInt)
  }

  override def list(skip: Int, limit: Int): Future[Either[SMFUEerror, Seq[Programmer]]] = {
    val request: Request = Request(Method.Get, "/programmers")
    processRequest(request, processProgrammers)
  }

  override def docs(): Future[Either[SMFUEerror, Json]] = {
    val request: Request = Request(Method.Get, "/programmers/docs")
    processRequest(request, Right(_))
  }

  override def readSlow(): Future[Either[SMFUEerror, Programmer]] = {
    val request: Request = Request(Method.Get, "/programmers/slow")
    processRequest(request, processProgrammer)
  }

  private def processProgrammer(json: Json): Either[SMFUEerror, Programmer] =
    json.as[Programmer] match {
      case Left(e)       => Left(ParseException(apiName, e.getMessage))
      case Right(result) => Right(result)
    }

  private def processProgrammers(json: Json): Either[SMFUEerror, Seq[Programmer]] =
    json.as[Seq[Programmer]] match {
      case Left(e)       => Left(ParseException(apiName, e.getMessage))
      case Right(result) => Right(result)
    }

  override def processInt(json: Json): Either[SMFUEerror, Int] =
    json.as[Int] match {
      case Left(e)       => Left(CreationException(apiName, e.getMessage))
      case Right(result) => Right(result)
    }

}

object ProgrammersService {

  def apply(programmersClient: Service[Request, Response])(
      implicit ec: ExecutionContext): ProgrammersService =
    new ProgrammersService(programmersClient, "programmers-api")

}
