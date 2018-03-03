package com.lunatech.swagmyfinchup.integration.filters

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.twitter.finagle.http._

final case class HeaderValidator(headerKey: String, expectedValue: String)
    extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val maybeHeader = request.headerMap.get(headerKey)
    println(s"Is the $headerKey header set? ${maybeHeader.isDefined}")
    maybeHeader match {
      case Some(header) =>
        if (header.equalsIgnoreCase(expectedValue)) service(request)
        else Future.value(Response(Status.Unauthorized))
      case None => Future.value(Response(Status.NotFound))
    }
  }
}

object HeaderValidator {}
