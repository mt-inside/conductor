package uk.org.empty.conductor.middleend.monitor

import akka.actor._
import scala.concurrent.duration._

import uk.org.empty.conductor.frontend.model._

object StatFetcher
{
  /* Props */
  def props(s: String) = Props(classOf[StatFetcher], s)

  /* Received events */

  /* Sent events */
}

class StatFetcher(s: String) extends Actor with ActorLogging
{
  import StatFetcher._

  def receive =
  {
    case _ => log.error("wtf")
  }
}
