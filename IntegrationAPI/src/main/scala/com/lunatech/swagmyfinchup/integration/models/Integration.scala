package com.lunatech.swagmyfinchup.integration.models

import java.util.UUID

import slick.driver.H2Driver.api._

case class Integration(id: UUID, programmerId: UUID, skillId: UUID, level: Int)

class Integrations(tag: Tag) extends Table[Integration](tag, "INTEGRATION") {
  def id           = column[UUID]("ID", O.PrimaryKey)
  def programmerId = column[UUID]("PROGRAMMERID")
  def skillId      = column[UUID]("SKILLID")
  def level        = column[Int]("LEVEL")
  def *            = (id, programmerId, skillId, level) <> (Integration.tupled, Integration.unapply)
}
