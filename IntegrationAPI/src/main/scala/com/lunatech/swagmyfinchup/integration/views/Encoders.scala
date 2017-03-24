package com.lunatech.swagmyfinchup.integration.views

import io.circe.{Encoder, Json}

trait Encoders {

  implicit val encodeExceptionCirce: Encoder[Exception] = Encoder.instance(
    e =>
      Json.obj(
        "type"         -> Json.fromString(e.getClass.getSimpleName),
        "message"      -> Json.fromString(e.getMessage),
        "internalCode" -> Json.fromString(e.getLocalizedMessage),
        "timestamp"    -> Json.fromLong(new java.util.Date().getTime)
    ))

}
