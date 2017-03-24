package com.lunatech.swagmyfinchup.integration.filters

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.twitter.finagle.http._

case class HeaderValidator(headerKey: String, expectedValue: String)
    extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request)
  }
}

object HeaderValidator {}
