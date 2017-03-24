package com.lunatech.swagmyfinchup.skills.controllers

import java.util.UUID

import com.lunatech.swagmyfinchup.skills.models._
import com.lunatech.swagmyfinchup.skills.utils.Converters._
import com.twitter.util.{Future => TwitterFuture}
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object SqlController {

  val skills: TableQuery[Skills]                           = TableQuery[Skills]
  val db: _root_.slick.driver.H2Driver.backend.DatabaseDef = Database.forConfig("h2mem1")

  def createDatabase =
    db.run(
      DBIO.seq(skills.schema.create,
               skills += Skill(UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"), "Scala", "Programming"),
               skills += Skill(UUID.fromString("4f6cff39-885e-4a8b-a672-ade70b1e8251"), "Java", "Programming"),
               skills += Skill(UUID.fromString("f9eb40e9-8c90-49cd-bb5a-9272e878de9b"), "Play", "Programming"),
               skills += Skill(UUID.fromString("5ae06d68-790c-4517-af64-94c9bc2f0495"), "Finch", "Programming"),
               skills += Skill(UUID.fromString("d28c6de3-5cdf-4a7c-b4e5-7b8ec1827de3"), "Finagle", "Programming"),
               skills += Skill(UUID.fromString("8b64ab41-211f-413c-b28c-2d6ff9e41d85"), "SQL", "DB"),
               skills += Skill(UUID.fromString("5e9df4d3-e9c6-4e31-9d21-80d2c57e0d50"), "Neo4J", "DB"),
               skills += Skill(UUID.fromString("83c98340-0998-4c4a-b336-a520b9a2285a"), "Elastic Search", "DB"),
               skills += Skill(UUID.fromString("864ba165-8b89-41df-b533-d7016400b2b0"), "Cassandra", "DB"),
               skills += Skill(UUID.fromString("990e52d9-13c7-4291-b643-e54c0ac284cd"), "Finatra", "Programming"),
               skills += Skill(UUID.fromString("5e491153-839d-4e73-8d28-0cc9406db4d4"), "Swagger", "Documenting")))

  private def getByID(id: UUID) =
    skills.filter(_.id === id)

  def getSkill(id: UUID): TwitterFuture[Either[SMFUEerror, Skill]] =
    Try(db.run(getByID(id).result.map(x =>
      Either.cond(x.nonEmpty, x.head, ReadException("Not found"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ReadException(e.getMessage)))
    }

  def insertSkill(skill: Skill): TwitterFuture[Either[SMFUEerror, Skill]] =
    TwitterFuture {
      Try(db.run(skills += skill)) match {
        case Success(r) => Right(skill)
        case Failure(e) => Left(CreationException(e.getMessage))
      }
    }

  def deleteSkill(id: UUID): TwitterFuture[Either[SMFUEerror, Int]] =
    Try(db.run(getByID(id).delete.map(x => Either.cond(x > 0, x, DeleteException("Not deleted"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(DeleteException(e.getMessage)))
    }

  def listSkills(skip: Int, limit: Int): TwitterFuture[Either[SMFUEerror, Seq[Skill]]] =
    Try(
      db.run(
        skills
          .drop(skip)
          .take(limit)
          .result
          .map(x => Either.cond(x.nonEmpty, x, ListException("Empty list"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ListException(e.getMessage)))
    }

  def updateSkill(id: UUID,
                  skillToSkill: Skill => Skill): TwitterFuture[Either[SMFUEerror, Skill]] = {
    (getSkill(id) map {
      case Right(oldSkill) => {
        (deleteSkill(id) map {
          case Right(status) => {
            insertSkill(skillToSkill(oldSkill))
          }
          case Left(e) => TwitterFuture.value(Left(e))
        }).flatten
      }
      case Left(e) => TwitterFuture.value(Left(e))
    }).flatten
  }

}
