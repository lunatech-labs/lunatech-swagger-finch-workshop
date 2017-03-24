package com.lunatech.swagmyfinchup.programmers.filters

import com.twitter.finagle.http.filter.Cors
import com.twitter.util.Duration
import com.lunatech.swagmyfinchup.programmers.utils.Helpers._

trait CORSFilter {

  def corspolicy(config: CorsConfig): Cors.Policy = Cors.Policy(
    allowsOrigin = _ => config.origin,
    allowsMethods = _ => None,
    allowsHeaders = _ => config.headers
  )

}
