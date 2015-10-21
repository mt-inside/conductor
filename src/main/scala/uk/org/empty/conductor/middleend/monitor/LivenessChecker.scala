package uk.org.empty.conductor.middleend.monitor

import akka.actor._
import scala.concurrent.duration._
import consul.Consul


object LivenessChecker
{
  /* Props */
  def props(s: String) = Props(classOf[LivenessChecker], s)

  /* Received events */

  /* Sent events */
}

class LivenessChecker(s: String) extends Actor with ActorLogging
{
  import LivenessChecker._

  //val agent = new consul.Consul(CONSUL_IP, Some(CONSUL_PORT))
  //import agent.v1._ as foo

  def receive =
  {
    case _ => log.error("wtf")
  }
}
