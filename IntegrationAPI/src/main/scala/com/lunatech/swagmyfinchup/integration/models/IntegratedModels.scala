package com.lunatech.swagmyfinchup.integration.models

import java.util.UUID

case class Integrated(id: UUID, programmer: Programmer, skill: Skill, level: Int)
case class IntegratedSkill(id: UUID, skill: Skill, level: Int)
case class IntegratedProgrammer(id: UUID, programmer: Programmer, level: Int)
