package com.lunatech.swagmyfinchup.skills.models

import java.util.UUID

import slick.jdbc.H2Profile.api._

final case class Skill(id: UUID, name: String, domain: String) extends Equals

class Skills(tag: Tag) extends Table[Skill](tag, "SKILL") {
  def id     = column[UUID]("ID", O.PrimaryKey)
  def name   = column[String]("NAME")
  def domain = column[String]("DOMAIN")

  def * = (id, name, domain) <> (Skill.tupled, Skill.unapply)
}
