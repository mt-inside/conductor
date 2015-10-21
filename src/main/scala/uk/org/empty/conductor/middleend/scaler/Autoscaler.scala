package uk.org.empty.conductor

trait Autoscaler
{
  def target(stats: Iterable[Double]) : Double
}
