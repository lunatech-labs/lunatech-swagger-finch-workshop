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
               programmers += Programmer(UUID.randomUUID(), "Inaki", "De Tejada", "Inaki.de.tejada@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Erik", "Janssen", "Erik.Janssen@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Hari", "Bageski", "Hari.Bageski@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Dimitrios", "Charoulis", "Dimitrios.Charoulis@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Tarun", "Kumar", "Tarun.Kumar@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Victor", "Basso", "Victor.Basso@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Daan", "Hoogenboezem", "Daan.Hoogenboezem@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Elham", "Ghanbaryfar", "Elham.Ghanbaryfar@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Mahya", "Mirtar", "Mahya.Mirtar@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Anastasiia", "Pushkina", "Anastasiia.Pushkina@lunatech.com"),
               programmers += Programmer(UUID.randomUUID(), "Gustavo", "De Micheli", "Gustavo.de.Micheli@lunatech.com")))

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

}
