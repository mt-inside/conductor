/* TODO:
 * multiple subclasses of this.
 * Liveness check "name" should become a URI
 * - "consul:foo" for consul health check named foo
 * - "ping:ip" to ping ip
 * - etc. Throw error for unrecognised scheme. Allow plug-ins by class-loading
 *   jars (have a registry of check URI schemes to impls)
 */

package uk.org.empty.conductor.middleend.monitor

import akka.actor._
import scala.concurrent.duration._
import scala.util.{Success,Failure}
import java.net.{InetAddress, Inet4Address}
import consul.Consul
import scala.collection.JavaConversions._


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


  // totes works, start actually doing some checking! (may require starting
  // machines first)
  val consul = new Consul("http://consul", 8500)
  for( dc <- consul.catalog().datacenters() )
  {
    for( n <- dc.nodes() )
    {
      //log.error(s"Datacenter ${dc.getName()}, node ${n.getName()}")
    }
  }

  def receive =
  {
    case _ => log.error("wtf")
  }
}
