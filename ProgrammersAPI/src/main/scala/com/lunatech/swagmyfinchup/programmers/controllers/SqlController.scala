package com.lunatech.swagmyfinchup.programmers.controllers

import java.util.UUID

import com.lunatech.swagmyfinchup.programmers.models._
import com.lunatech.swagmyfinchup.programmers.utils.Converters._
import com.twitter.util.{Future => TwitterFuture}
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object SqlController {

  val programmers: TableQuery[Programmers]                 = TableQuery[Programmers]
  val db: _root_.slick.driver.H2Driver.backend.DatabaseDef = Database.forConfig("h2mem1")

  def createDatabase =
    db.run(
      DBIO.seq(programmers.schema.create,
               programmers += Programmer(UUID.fromString("4facbbad-c0cc-4eb3-9750-19c3f655ba47"), "Inaki", "De Tejada", "Inaki.de.tejada@lunatech.com"),
               programmers += Programmer(UUID.fromString("448a6819-4081-4642-8c16-aec008278c8f"), "Erik", "Janssen", "Erik.Janssen@lunatech.com"),
               programmers += Programmer(UUID.fromString("f125df7d-c1fe-42e3-985f-68c532c7c4db"), "Hari", "Bageski", "Hari.Bageski@lunatech.com"),
               programmers += Programmer(UUID.fromString("7878f7b5-18c5-4f21-a389-6ff7404f78bc"), "Dimitrios", "Charoulis", "Dimitrios.Charoulis@lunatech.com"),
               programmers += Programmer(UUID.fromString("03b6b2d1-5def-4b92-add3-343977979c8b"), "Tarun", "Kumar", "Tarun.Kumar@lunatech.com"),
               programmers += Programmer(UUID.fromString("65928b88-6712-4163-9ac3-afa465c81abb"), "Victor", "Basso", "Victor.Basso@lunatech.com"),
               programmers += Programmer(UUID.fromString("d091a0b8-94d9-4707-84f6-d4434710a56f"), "Daan", "Hoogenboezem", "Daan.Hoogenboezem@lunatech.com"),
               programmers += Programmer(UUID.fromString("6aa9a17c-9c10-46d0-83ec-bf40ac666cba"), "Elham", "Ghanbaryfar", "Elham.Ghanbaryfar@lunatech.com"),
               programmers += Programmer(UUID.fromString("4c2e4dfa-713d-4a82-aff1-ca5e1cfc39c3"), "Mahya", "Mirtar", "Mahya.Mirtar@lunatech.com"),
               programmers += Programmer(UUID.fromString("a90ff3e2-f52b-4d83-a906-4cdefd37b0a3"), "Anastasiia", "Pushkina", "Anastasiia.Pushkina@lunatech.com"),
               programmers += Programmer(UUID.fromString("d4708ee5-9149-4750-9d09-7d50fd2ad0c4"), "Gustavo", "De Micheli", "Gustavo.de.Micheli@lunatech.com")))

  private def getByID(id: UUID) =
    programmers.filter(_.id === id)

  def getProgrammer(id: UUID): TwitterFuture[Either[SMFUEerror, Programmer]] =
    Try(db.run(getByID(id).result.map(x =>
      Either.cond(x.nonEmpty, x.head, ReadException("Not found"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ReadException(e.getMessage)))
    }

  def insertProgrammer(programmer: Programmer): TwitterFuture[Either[SMFUEerror, Programmer]] =
    TwitterFuture {
      Try(db.run(programmers += programmer)) match {
        case Success(r) => Right(programmer)
        case Failure(e) => Left(CreationException(e.getMessage))
      }
    }

  def deleteProgrammer(id: UUID): TwitterFuture[Either[SMFUEerror, Int]] =
    Try(db.run(getByID(id).delete.map(x =>
      Either.cond(x > 0, x, DeleteException("Not deleted"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(DeleteException(e.getMessage)))
    }

  def listProgrammers(skip: Int, limit: Int): TwitterFuture[Either[SMFUEerror, Seq[Programmer]]] =
    Try(
      db.run(
        programmers
          .drop(skip)
          .take(limit)
          .result
          .map(x => Either.cond(x.nonEmpty, x, ListException("Empty list"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ListException(e.getMessage)))
    }

  def updateProgrammer(id: UUID, programmerToProgrammer: Programmer => Programmer)
    : TwitterFuture[Either[SMFUEerror, Programmer]] = {
    (getProgrammer(id) map {
      case Right(oldProgrammer) => {
        (deleteProgrammer(id) map {
          case Right(status) => {
            insertProgrammer(programmerToProgrammer(oldProgrammer))
          }
          case Left(e) => TwitterFuture.value(Left(e))
        }).flatten
      }
      case Left(e) => TwitterFuture.value(Left(e))
    }).flatten
  }

  def insertProgrammers(insert: Seq[Programmer]): TwitterFuture[Either[SMFUEerror, Seq[Programmer]]] =
    TwitterFuture {
      Try(db.run(programmers ++= insert)) match {
        case Success(r) => Right(insert)
        case Failure(e) => Left(CreationException(e.getMessage))
      }
    }

}
