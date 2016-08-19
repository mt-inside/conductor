/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor

import enrichments._

class PScaler extends Autoscaler
{
  def target(stats: Iterable[Double]) : Double = stats.sum
}
