/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.middleend.monitor

/* Common messages across all monitor actors */
object MonitorProtocol
{
  final case class ScaleTarget(instances: Double)
  final case class Liveness(ok: Boolean)
  final case object Stop
  final case object Stopped
}
