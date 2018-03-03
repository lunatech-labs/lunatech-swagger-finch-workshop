package com.lunatech.swagmyfinchup.integration.controllers

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.models._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.netty3.ChannelBufferBuf
import com.twitter.util.Future
import io.circe.Json
import io.circe.parser._
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.util.CharsetUtil._

trait APIService[T] {

  def client: Service[Request, Response]
  def apiName: String
  def create(registry: T): Future[Either[SMFUEerror, T]]
  def read(id: UUID): Future[Either[SMFUEerror, T]]
  def update(id: UUID, registryF: T => T): Future[Either[SMFUEerror, T]]
  def delete(id: UUID): Future[Either[SMFUEerror, Int]]
  def list(skip: Int, limit: Int): Future[Either[SMFUEerror, Seq[T]]]
  def docs(): Future[Either[SMFUEerror, Json]]
  def readSlow(): Future[Either[SMFUEerror, T]]

  protected def buildRequest(method: Method, uri: String, registry: Option[Json]): Request = {
    val request: Request = Request(method, uri)
    if (registry.isDefined)
      request.content = ChannelBufferBuf.newOwned(copiedBuffer(registry.get.toString(), UTF_8))
    request
  }

  protected def processInt(json: Json): Either[SMFUEerror, Int] =
    json.as[Int] match {
      case Left(e)       => Left(CreationException("generic", e.getMessage))
      case Right(result) => Right(result)
    }

  protected def processRequest[A](request: Request,
                                  f: Json => Either[SMFUEerror, A]): Future[Either[SMFUEerror, A]] =
    client(request) map (resp => {
      if (resp.statusCode >= 400)
        Left(
          CreationException(apiName,
                            s"$apiName error: ${resp.statusCode} - ${resp.getContentString}"))
      else {
        parse(resp.contentString) match {
          case Left(e)     => Left(CreationException(apiName, e.getMessage))
          case Right(json) => f(json)
        }
      }
    })

}
