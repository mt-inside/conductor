/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor

import java.time._

import enrichments._

class SimpleScaler extends Autoscaler
{
  var target : Double = 0
  var lastResponse = Instant.now

  def target(loads: Iterable[Double]) : Double =
  {
    if (Duration.between(lastResponse, Instant.now).getSeconds >= 10)
    {
      lastResponse = Instant.now
      val avg = loads.average
      // Expression returns Unit...
      if (avg >= 1.1) target += 1.0
      else if (avg <= 0.9) target -= 1.0
    }
    target
  }
}
