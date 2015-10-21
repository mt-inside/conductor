package uk.org.empty.conductor.middleend.monitor

/* Common messages across all monitor actors */
object MonitorProtocol
{
  final case class ScaleTarget(instances: Double)
  final case class Liveness(ok: Boolean)
}
