package com.lunatech.swagmyfinchup.integration.models


sealed trait SMFUEerror extends Exception {
  override def getLocalizedMessage: String = internalCode.toString
  def timestamp: Long                      = new java.util.Date().getTime()
  def internalCode: Int
  def httpCode: Int
}

case class CreationException(modelName: String, cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not create $modelName. Cause: $cause"
  override def internalCode: Int  = 1
  override def httpCode: Int      = 500
}

case class ReadException(modelName: String, cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not Read $modelName. Cause: $cause"
  override def internalCode: Int  = 2
  override def httpCode: Int      = 404
}

case class UpdateException(modelName: String, cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not update $modelName. Cause: $cause"
  override def internalCode: Int  = 3
  override def httpCode: Int      = 500
}

case class DeleteException(modelName: String, cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not delete $modelName. Cause: $cause"
  override def internalCode: Int  = 4
  override def httpCode: Int      = 500
}

case class ListException(modelName: String, cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not list $modelName. Cause: $cause"
  override def internalCode: Int  = 5
  override def httpCode: Int      = 500
}

case class ParseException(modelName: String, cause: String) extends SMFUEerror {
  override def getMessage: String = s"Could not parse $modelName. Cause: $cause"
  override def internalCode: Int  = 6
  override def httpCode: Int      = 500
}
