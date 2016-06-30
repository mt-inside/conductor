package uk.org.empty.conductor.middleend.monitor

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import uk.org.empty.conductor.frontend.controller.CrudMessages
import uk.org.empty.conductor.frontend.model.{Definition, Group}
import uk.org.empty.conductor.frontend.event.UserEvent

/* TODO: not got many features atm.
 * - needs to receive and send liveness and maybe target
 * - needs to do dependencies. For start / restart, have a Starting state with a
 *   Starting data that contains a queue of the things remaining. pop one in
 *   response to every MachineStarted
 */
object GroupMonitor
{
  /* Props */
  def props(g: Group, instanceId: Int) = Props(classOf[GroupMonitor], g, instanceId)

  /* Received and Sent events */
  // MonitorProtocol._

  /* States */
  sealed trait State
  case object Inactive extends State
  case object Active extends State

  /* Data */
  sealed trait Data
  final case class Children(
    /* Own */
    checks: Map[String, ActorRef], // Clusters
    // Groups aren't concerned with load
    /* Receive */
    livenesses: Map[ActorRef, Boolean]
    // Groups aren't concerned with load
  ) extends Data
}

class GroupMonitor(g: Group, instanceId: Int) extends LoggingFSM[GroupMonitor.State, GroupMonitor.Data]
{
  import GroupMonitor._
  import MonitorProtocol._

  def getDefn(name: String) =
  {
    import context.dispatcher
    implicit val timeout = Timeout(10.seconds)

    val defS = context.actorSelection("/user/Stores/Definition")
    val defn = (defS ? CrudMessages.Show(name))
      .mapTo[CrudMessages.Details[Definition]]
      .map { d => d.obj }

    Await.result(defn, 1.second)
  }

  /* Start the (clusters of) children */
  val checks = g.children.map { c => (c -> context.actorOf(ClusterMonitor.props(getDefn(c)), s"cluster-$c")) }.toMap
  // Stats collection is not managed by this actor; the Cluster will have started StatsAggregators as our siblings.

  /* Populate initial set of liveness values */
  val livenesses = checks.map { c => (c._2 -> false) }.toMap
  // Stats are not reported into this actor; the Cluster will have started StatsAggregators as our siblings.

  startWith(Inactive, Children(checks, livenesses))


  // factor out with the very similar code from cluster, along with target aggregation
  when(Inactive)
  {
    case Event(Liveness(ok), s: Children) =>
    {
      val newLives = s.livenesses.updated(sender, ok)
      val newState = s.copy(livenesses = newLives)

      if (newLives.values.forall(ok => ok))
        goto(Active) using newState
      else
        stay using newState
    }
  }

  when(Active)
  {
    case Event(Liveness(ok), s: Children) =>
    {
      val newLives = s.livenesses.updated(sender, ok)
      val newState = s.copy(livenesses = newLives)

      if (!newLives.values.forall(ok => ok))
        goto(Inactive) using newState
      else
        stay using newState
    }
  }


  whenUnhandled
  {
    case Event(Stop, s: Children) =>
    {
      s.checks.values.foreach{ c => c ! Stop }
      stay // Above is a graceful stop message so we don't just declare ourself Inactive yet, but wait for that to propage up from the instances.
    }
  }


  onTransition
  {
    /* Only matches the first one */
    case _ -> Inactive =>
    {
      context.system.eventStream.publish(UserEvent(s"Group Inactive ${g.name}"))
    }
    case _ -> Active =>
    {
      context.system.eventStream.publish(UserEvent(s"Group Active ${g.name}"))
    }
  }


  initialize
}
