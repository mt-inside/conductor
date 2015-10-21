package uk.org.empty.conductor

import enrichments._

class PScaler extends Autoscaler
{
  def target(stats: Iterable[Double]) : Double = stats.sum
}
