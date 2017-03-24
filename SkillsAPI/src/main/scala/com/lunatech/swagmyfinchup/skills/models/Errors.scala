package com.lunatech.swagmyfinchup.skills.models

import io.circe.{Encoder, Json}

sealed trait SMFUEerror extends Exception {
  override def getLocalizedMessage: String = internalCode.toString
  def timestamp: Long                      = new java.util.Date().getTime()
  def internalCode: Int
  def httpCode: Int
}

case class CreationException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not create skills. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}

case class ReadException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not Read skills. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 404
}

case class UpdateException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not update skills. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}

case class DeleteException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not delete skills. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}

case class ListException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not list skills. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}
