package extetris.config

final case class ValidationError(message: String) extends Exception(message):
  override def fillInStackTrace(): ValidationError = this