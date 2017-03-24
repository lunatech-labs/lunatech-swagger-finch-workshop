package com.lunatech.swagmyfinchup.programmers.models

import java.util.UUID

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._
import slick.driver.H2Driver.api._

case class Programmer(id: UUID, name: String, lastName: String, email: String) //add email

object Programmer extends ((UUID, String, String, String) => Programmer) {

  implicit val encodeProgrammer = new Encoder[Programmer] {
    override def apply(programmer: Programmer): Json = Json.obj(
      "id"       -> programmer.id.asJson,
      "name"     -> programmer.name.asJson,
      "lastName" -> programmer.lastName.asJson,
      "email"    -> programmer.email.asJson
    )
  }

  implicit val personDecoder: Decoder[Programmer] = deriveDecoder

}

class Programmers(tag: Tag) extends Table[Programmer](tag, "PROGRAMMER") {
  def id       = column[UUID]("ID", O.PrimaryKey)
  def name     = column[String]("NAME")
  def lastName = column[String]("LASTNAME")
  def email    = column[String]("EMAIL")
  def *        = (id, name, lastName, email) <> (Programmer.tupled, Programmer.unapply)
}
