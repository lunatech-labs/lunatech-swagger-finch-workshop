package com.lunatech.swagmyfinchup.integration.controllers

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.models._
import com.twitter.util.{Future => TwitterFuture}
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success, Try}
import com.lunatech.swagmyfinchup.integration.utils.Converters._
import com.lunatech.swagmyfinchup.integration.models._

object SqlController {

  val integrations: TableQuery[Integrations] = TableQuery[Integrations]
  val db                                     = Database.forConfig("h2mem1")

  def createDatabase =
    db.run(
      DBIO.seq(
        integrations.schema.create,
        integrations += Integration(
          UUID.fromString("742dff95-8618-4d70-946c-a78e0a184d42"),
          UUID.fromString("4facbbad-c0cc-4eb3-9750-19c3f655ba47"),
          UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
          Random.nextInt(10)
        ),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("448a6819-4081-4642-8c16-aec008278c8f"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("f125df7d-c1fe-42e3-985f-68c532c7c4db"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("7878f7b5-18c5-4f21-a389-6ff7404f78bc"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("03b6b2d1-5def-4b92-add3-343977979c8b"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("65928b88-6712-4163-9ac3-afa465c81abb"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("d091a0b8-94d9-4707-84f6-d4434710a56f"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("6aa9a17c-9c10-46d0-83ec-bf40ac666cba"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("4c2e4dfa-713d-4a82-aff1-ca5e1cfc39c3"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("a90ff3e2-f52b-4d83-a906-4cdefd37b0a3"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("d4708ee5-9149-4750-9d09-7d50fd2ad0c4"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("4facbbad-c0cc-4eb3-9750-19c3f655ba47"),
                                    UUID.fromString("4f6cff39-885e-4a8b-a672-ade70b1e8251"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("4facbbad-c0cc-4eb3-9750-19c3f655ba47"),
                                    UUID.fromString("5e491153-839d-4e73-8d28-0cc9406db4d4"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("448a6819-4081-4642-8c16-aec008278c8f"),
                                    UUID.fromString("f9eb40e9-8c90-49cd-bb5a-9272e878de9b"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("448a6819-4081-4642-8c16-aec008278c8f"),
                                    UUID.fromString("8b64ab41-211f-413c-b28c-2d6ff9e41d85"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("f125df7d-c1fe-42e3-985f-68c532c7c4db"),
                                    UUID.fromString("5ae06d68-790c-4517-af64-94c9bc2f0495"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("f125df7d-c1fe-42e3-985f-68c532c7c4db"),
                                    UUID.fromString("5e9df4d3-e9c6-4e31-9d21-80d2c57e0d50"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("7878f7b5-18c5-4f21-a389-6ff7404f78bc"),
                                    UUID.fromString("d28c6de3-5cdf-4a7c-b4e5-7b8ec1827de3"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("7878f7b5-18c5-4f21-a389-6ff7404f78bc"),
                                    UUID.fromString("83c98340-0998-4c4a-b336-a520b9a2285a"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("03b6b2d1-5def-4b92-add3-343977979c8b"),
                                    UUID.fromString("8b64ab41-211f-413c-b28c-2d6ff9e41d85"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("03b6b2d1-5def-4b92-add3-343977979c8b"),
                                    UUID.fromString("83c98340-0998-4c4a-b336-a520b9a2285a"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("65928b88-6712-4163-9ac3-afa465c81abb"),
                                    UUID.fromString("5e9df4d3-e9c6-4e31-9d21-80d2c57e0d50"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("65928b88-6712-4163-9ac3-afa465c81abb"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("d091a0b8-94d9-4707-84f6-d4434710a56f"),
                                    UUID.fromString("83c98340-0998-4c4a-b336-a520b9a2285a"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("d091a0b8-94d9-4707-84f6-d4434710a56f"),
                                    UUID.fromString("5e491153-839d-4e73-8d28-0cc9406db4d4"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("6aa9a17c-9c10-46d0-83ec-bf40ac666cba"),
                                    UUID.fromString("864ba165-8b89-41df-b533-d7016400b2b0"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("6aa9a17c-9c10-46d0-83ec-bf40ac666cba"),
                                    UUID.fromString("8b64ab41-211f-413c-b28c-2d6ff9e41d85"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("4c2e4dfa-713d-4a82-aff1-ca5e1cfc39c3"),
                                    UUID.fromString("990e52d9-13c7-4291-b643-e54c0ac284cd"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("4c2e4dfa-713d-4a82-aff1-ca5e1cfc39c3"),
                                    UUID.fromString("8b64ab41-211f-413c-b28c-2d6ff9e41d85"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("a90ff3e2-f52b-4d83-a906-4cdefd37b0a3"),
                                    UUID.fromString("5e491153-839d-4e73-8d28-0cc9406db4d4"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("a90ff3e2-f52b-4d83-a906-4cdefd37b0a3"),
                                    UUID.fromString("5e9df4d3-e9c6-4e31-9d21-80d2c57e0d50"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("d4708ee5-9149-4750-9d09-7d50fd2ad0c4"),
                                    UUID.fromString("4b423060-2611-4f60-9a8c-73356da53661"),
                                    Random.nextInt(10)),
        integrations += Integration(UUID.randomUUID(),
                                    UUID.fromString("d4708ee5-9149-4750-9d09-7d50fd2ad0c4"),
                                    UUID.fromString("5e491153-839d-4e73-8d28-0cc9406db4d4"),
                                    Random.nextInt(10))
      ))

  private def getByID(id: UUID) =
    integrations.filter(_.id === id)

  private def getByProgrammer(id: UUID) =
    integrations.filter(_.programmerId === id)

  private def getBySkill(id: UUID) =
    integrations.filter(_.skillId === id)

  def getIntegration(id: UUID): TwitterFuture[Either[SMFUEerror, Integration]] =
    Try(db.run(getByID(id).result.map(x =>
      Either.cond(x.nonEmpty, x.head, ReadException("Integration", "Not found"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ReadException("Integration", e.getMessage)))
    }

  def insertIntegration(programmer: Integration): TwitterFuture[Either[SMFUEerror, Integration]] =
    TwitterFuture {
      Try(db.run(integrations += programmer)) match {
        case Success(_) => Right(programmer)
        case Failure(e) => Left(CreationException("Integration", e.getMessage))
      }
    }

  def deleteIntegration(id: UUID): TwitterFuture[Either[SMFUEerror, Int]] =
    Try(db.run(getByID(id).delete.map(x =>
      Either.cond(x > 0, x, DeleteException("Integration", "Not deleted"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(DeleteException("Integration", e.getMessage)))
    }

  def listIntegrations(skip: Int, limit: Int): TwitterFuture[Either[SMFUEerror, Seq[Integration]]] =
    Try(
      db.run(integrations
        .drop(skip)
        .take(limit)
        .result
        .map(x => Either.cond(x.nonEmpty, x, ListException("Integration", "Empty list"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ListException("Integration", e.getMessage)))
    }

  def getSkillsForProgrammer(id: UUID,
                             skip: Int,
                             limit: Int): TwitterFuture[Either[SMFUEerror, Seq[Integration]]] =
    Try(
      db.run(getByProgrammer(id)
        .drop(skip)
        .take(limit)
        .result
        .map(x => Either.cond(x.nonEmpty, x, ListException("Integration", "Empty list"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ListException("Integration", e.getMessage)))
    }

  def getProgrammersForSkill(id: UUID,
                             skip: Int,
                             limit: Int): TwitterFuture[Either[SMFUEerror, Seq[Integration]]] =
    Try(
      db.run(getBySkill(id)
        .drop(skip)
        .take(limit)
        .result
        .map(x => Either.cond(x.nonEmpty, x, ListException("Integration", "Empty list"))))) match {
      case Success(r) => r.asTwitter
      case Failure(e) => TwitterFuture.value(Left(ListException("Integration", e.getMessage)))
    }

  def updateIntegration(id: UUID, programmerToIntegration: Integration => Integration)
    : TwitterFuture[Either[SMFUEerror, Integration]] = {
    (getIntegration(id) map {
      case Right(oldIntegration) => {
        (deleteIntegration(id) map {
          case Right(_) => {
            insertIntegration(programmerToIntegration(oldIntegration))
          }
          case Left(e) => TwitterFuture.value(Left(e))
        }).flatten
      }
      case Left(e) => TwitterFuture.value(Left(e))
    }).flatten
  }

}
