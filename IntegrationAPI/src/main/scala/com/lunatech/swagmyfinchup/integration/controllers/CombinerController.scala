package com.lunatech.swagmyfinchup.integration.controllers

import java.util.UUID

import com.lunatech.swagmyfinchup.integration.models._
import com.twitter.util.Future

object CombinerController {

  def createIntegration(integration: Integration,
                        programmersService: APIService[Programmer],
                        skillsService: APIService[Skill]): Future[Either[SMFUEerror, Integrated]] =
    for {
      programmerE  <- programmersService read (integration programmerId)
      skillE       <- skillsService read (integration skillId)
      integrationF <- create(integration, programmerE, skillE)
    } yield integrationF

  private def create(integration: Integration,
                     programmerE: Either[SMFUEerror, Programmer],
                     skillE: Either[SMFUEerror, Skill]): Future[Either[SMFUEerror, Integrated]] =
    SqlController insertIntegration integration map {
      case Left(e)      => Left(e)
      case Right(intgr) => createIntegrated(programmerE, skillE, intgr.level, intgr.id)
    }

  def readIntegration(id: UUID,
                      programmersService: APIService[Programmer],
                      skillsService: APIService[Skill]): Future[Either[SMFUEerror, Integrated]] =
    for {
      integrationE <- SqlController getIntegration id
      integrated <- if (integrationE.isRight)
        getIntegrated(integrationE.right.get.programmerId,
                      integrationE.right.get.skillId,
                      integrationE.right.get.level,
                      id,
                      programmersService read,
                      skillsService read)
      else Future.value(Left(integrationE.left.get))
    } yield integrated

  private def getIntegrated(programmer: UUID,
                            skill: UUID,
                            level: Int,
                            id: UUID,
                            getProgrammer: (UUID) => Future[Either[SMFUEerror, Programmer]],
                            getSkill: (UUID) => Future[Either[SMFUEerror, Skill]])
    : Future[Either[SMFUEerror, Integrated]] =
    for {
      programmerE <- getProgrammer(programmer)
      skillE      <- getSkill(skill)
      integrated  <- Future.value(createIntegrated(programmerE, skillE, level, id))
    } yield integrated

  private def createIntegrated(programmerE: Either[SMFUEerror, Programmer],
                               skillE: Either[SMFUEerror, Skill],
                               level: Int,
                               id: UUID): Either[SMFUEerror, Integrated] =
    if (programmerE.isRight && skillE.isRight)
      Right(Integrated(id, programmerE.right.get, skillE.right.get, level))
    else Left(ReadException("Integrated", "A value doesnt exist"))

  def updateIntegration(
      id: UUID,
      integrationToIntegration: Integration => Integration,
      programmersService: APIService[Programmer],
      skillsService: APIService[Skill]): Future[Either[SMFUEerror, Integrated]] = {
    val dummyUUID        = UUID.randomUUID()
    val dummyIntegration = integrationToIntegration(Integration(id, dummyUUID, dummyUUID, 0))
    for {
      programmerE  <- programmersService read (dummyIntegration programmerId)
      skillE       <- skillsService read (dummyIntegration skillId)
      integrationF <- update(id, integrationToIntegration, programmerE, skillE)
    } yield integrationF
  }

  private def update(id: UUID,
                     integrationToIntegration: Integration => Integration,
                     programmerE: Either[SMFUEerror, Programmer],
                     skillE: Either[SMFUEerror, Skill]): Future[Either[SMFUEerror, Integrated]] =
    SqlController updateIntegration (id, integrationToIntegration) map {
      case Left(e)      => Left(e)
      case Right(intgr) => createIntegrated(programmerE, skillE, intgr.level, intgr.id)
    }

  def readIntegrationsForProgrammer(
      id: UUID,
      skip: Int,
      limit: Int,
      skillsService: APIService[Skill]): Future[Either[SMFUEerror, Seq[IntegratedSkill]]] = {

    (SqlController getSkillsForProgrammer (id, skip, limit) map {
      case Right(integrations) =>
        Future
          .collect(
            integrations.map(integration =>
              asSkillIntegration(integration.id,
                                 integration.level,
                                 skillsService.read(integration.skillId))))
          .map(flattenEither) map {
          case Right(skills) => Right(skills)
          case Left(e)       => Left(e)
        }
      case Left(e) => Future.value(Left(e))
    }).flatten

  }

  def asSkillIntegration(
      id: UUID,
      level: Int,
      skillE: Future[Either[SMFUEerror, Skill]]): Future[Either[SMFUEerror, IntegratedSkill]] = {
    skillE map {
      case Left(e)      => Left(e)
      case Right(skill) => Right(IntegratedSkill(id, skill, level))
    }
  }

  def asProgrammerIntegration(id: UUID,
                              level: Int,
                              programmerE: Future[Either[SMFUEerror, Programmer]])
    : Future[Either[SMFUEerror, IntegratedProgrammer]] = {
    programmerE map {
      case Left(e)      => Left(e)
      case Right(skill) => Right(IntegratedProgrammer(id, skill, level))
    }
  }

  def readIntegrationsForSkill(id: UUID,
                               skip: Int,
                               limit: Int,
                               programmersService: APIService[Programmer])
    : Future[Either[SMFUEerror, Seq[IntegratedProgrammer]]] = {

    (SqlController getProgrammersForSkill (id, skip, limit) map {
      case Right(integrations) =>
        Future
          .collect(
            integrations.map(integration =>
              asProgrammerIntegration(integration.id,
                                      integration.level,
                                      programmersService.read(integration.programmerId))))
          .map(flattenEither) map {
          case Right(programmers) => Right(programmers)
          case Left(e)            => Left(e)
        }
      case Left(e) => Future.value(Left(e))
    }).flatten

  }

  private[this] def flattenEither[A, B](x: Seq[Either[A, B]]): Either[A, Seq[B]] =
    (x foldRight (Right(Nil): Either[A, Seq[B]])) { (e, acc) =>
      for (xs <- acc.right; x <- e.right) yield x +: xs
    }

}
