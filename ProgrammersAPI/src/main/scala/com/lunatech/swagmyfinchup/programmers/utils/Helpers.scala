package com.lunatech.swagmyfinchup.programmers.utils

import com.ccadllc.cedi.config.ConfigErrors

object Helpers {

  def validateConfig[A](confs: Either[ConfigErrors, A]) =
    confs match {
      case Left(errors) => sys.error(s"Error(s) in config : $errors")
      case Right(c)     => c
    }

  final case class CorsConfig(origin: Option[String],
                              methods: Option[List[String]],
                              headers: Option[List[String]])
}
