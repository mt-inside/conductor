package uk.org.empty.conductor.middleend.monitor

import akka.actor._
import scala.concurrent.duration._

import uk.org.empty.conductor.frontend.model._

object StatsAggregator
{
  /* Props */
  def props(s: String) = Props(classOf[StatsAggregator], s)

  /* Received events */

  /* Sent events */
}

class StatsAggregator(s: String) extends Actor with ActorLogging
{
  import StatsAggregator._

  def receive =
  {
    case _ => log.error("wtf")
  }
}
