package com.lunatech.swagmyfinchup.integration.utils

import javax.net.ssl.SSLContext

import com.twitter.finagle._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.param.HighResTimer
import com.twitter.finagle.service.{Backoff, RetryFilter}
import com.twitter.util.{Duration, Return, Try}
import com.twitter.conversions.time._
import com.twitter.finagle.Http.Client
import com.twitter.finagle.client.DefaultPool
import com.twitter.finagle.http.param.Streaming
import com.twitter.finagle.http.service.HttpResponseClassifier
import org.slf4j.{Logger, LoggerFactory}

object ClientFactory {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def apply(label: String,
            instances: Name,
            sslContextOpt: Option[SSLContext]): Service[Request, Response] = {
    val shouldRetry: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Return(rep)) => rep.status.code != 200
    }
    implicit val t = HighResTimer.Default
    val retryFilter: RetryFilter[Request, Response] =
      RetryFilter(Backoff.const(1.second).take(3))(shouldRetry)
    Http.client
      .filtered(retryFilter)
      .withTls(sslContextOpt)
      .withFailFast(false)
      .configured(Streaming(enabled = true))
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .configured(Http.Netty4Impl)
      .newService(instances, label)
  }

  implicit class ClientOps(client: Http.Client) {

    def withTls(sslContextOpt: Option[SSLContext]) =
      sslContextOpt
        .map { sslContex =>
          client.withTransport.tls(sslContex)
        }
        .getOrElse(client)
    def withFailFast(failFast: Boolean): Client =
      if (failFast) client else client.withSessionQualifier.noFailFast

    def configurePool(low: Int, high: Int, bufferSize: Int, idleTime: Int, maxWaiters: Int) =
      client.configured(
        DefaultPool
          .Param(low, high, bufferSize, idleTime = Duration.fromMilliseconds(idleTime), maxWaiters))

  }

//  implicit class FinagleResponseHandler(respF: Future[Response]) {
//
//    def handleResponse[SMFUType: Decoder](serviceName: String,
//                                                  endpointName: String): Future[Either[SMFUEerror, SMFUType]] =
//      handleResponse[SMFUType, SMFUType](identity,
//        serviceName,
//        endpointName)
//
//    def handleResponse[ProviderType: Decoder, SMFUType](transform: ProviderType => SMFUType,
//                                                        apiName: String,
//                                                        endpointName: String): Future[Either[SMFUEerror, SMFUType]] = {
//      handleThrowable(apiName).map((resp: Response) => {
//
//        statsReceiver
//          .counter(s"${apiName}.${endpointName}.HTTPResponses.${resp.statusCode}")
//          .incr()
//
//        if (resp.statusCode < 400) {
//          parse(resp.contentString) match {
//            case Left(e) =>
//              logger.error(s"Failed to parse json: ${resp.contentString}", e)
//              Left(CreationException(apiName, e.getMessage))
//            case Right(aa) =>
//              Right(transform(aa))
//          }
//        } else if (resp.statusCode == 503)
//          Left(CreationException(
//            apiName,
//            s"$apiName error: ${resp.statusCode} - ${resp.getContentString}"))
//        else Left(CreationException(
//          apiName,
//          s"$apiName error: ${resp.statusCode} - ${resp.getContentString}"))
//
//      })
//    }
//
//    def handleThrowable(apiName: String): Future[Response] =
//      respF.handle {
//      case e: ChannelException =>
//        logger.error(s"Error on $apiName.ChannelException", e)
//        Response(Status.ClientClosedRequest)
//
//      case e: IndividualRequestTimeoutException =>
//        logger.error(s"Error on $apiName.IndividualRequestTimeoutException", e)
//        Response(Status.RequestTimeout)
//
//      case e: Throwable =>
//        logger.error(s"Error on $apiName.Exception", e)
//        Response(Status.InternalServerError)
//    }
//  }

}
