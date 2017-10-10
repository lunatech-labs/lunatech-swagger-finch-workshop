package com.lunatech.swagmyfinchup.programmers.models


sealed trait SMFUEerror extends Exception {
  override def getLocalizedMessage: String = internalCode.toString
  def timestamp: Long                      = new java.util.Date().getTime()
  def internalCode: Int
  def httpCode: Int
}

case class CreationException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not create programmer. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}

case class ReadException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not Read programmer. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 404
}

case class UpdateException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not update programmer. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}

case class DeleteException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not delete programmer. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}

case class ListException(cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not list programmers. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}
