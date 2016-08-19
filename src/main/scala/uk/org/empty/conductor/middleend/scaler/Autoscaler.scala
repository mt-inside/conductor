/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor

trait Autoscaler
{
  def target(stats: Iterable[Double]) : Double
}
