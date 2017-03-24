package com.lunatech.swagmyfinchup.integration.filters

import com.twitter.finagle.http.filter.Cors
import com.lunatech.swagmyfinchup.integration.utils.Helpers._

trait CORSFilter {

  def corspolicy(config: CorsConfig): Cors.Policy = Cors.Policy(
    allowsOrigin = _ => config.origin,
    allowsMethods = _ => config.methods,
    allowsHeaders = _ => config.headers
  )
}
