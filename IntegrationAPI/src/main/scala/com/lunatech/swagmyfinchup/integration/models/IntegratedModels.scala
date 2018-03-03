package com.lunatech.swagmyfinchup.integration.models

import java.util.UUID

final case class Integrated(id: UUID, programmer: Programmer, skill: Skill, level: Int)
final case class IntegratedSkill(id: UUID, skill: Skill, level: Int)
final case class IntegratedProgrammer(id: UUID, programmer: Programmer, level: Int)
