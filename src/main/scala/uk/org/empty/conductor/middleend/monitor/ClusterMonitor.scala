package uk.org.empty.conductor.middleend.monitor

import akka.actor._
import scala.concurrent.duration._

import uk.org.empty.conductor.frontend.model._
import uk.org.empty.conductor.frontend.event.UserEvent

object ClusterMonitor
{
  /* Props */
  def props(d: Definition) = Props(classOf[ClusterMonitor], d)

  /* Received and Sent events */
  // MonitorProtocol._

  /* States */
  sealed trait State
  case object Inactive extends State
  case object Active extends State
  case object Stopping extends State

  /* Data */
  sealed trait Data
  final case class Children(
    /* Own */
    checks: Map[Int, ActorRef], // Instance/Group Monitors
    stats: Map[String, ActorRef],
    /* Receive */
    livenesses: Map[ActorRef, Boolean],
    targets: Map[ActorRef, Double]
  ) extends Data
}

class ClusterMonitor(d: Definition) extends LoggingFSM[ClusterMonitor.State, ClusterMonitor.Data]
{
  import ClusterMonitor._
  import MonitorProtocol._

  /* Not sure livenesses and targets should be pre-populated.
   * - Advantage is that we can easily check that messages are coming from known
   * children, but it's not hard to check the values of the two primary maps, or
   * the actor's children list
   * - Liveness check is hard becuase to go active, all expected tests need to
   * have changed to passing. If a test hasn't reported in yet, it has to be
   * assumed to be failing. Would be OK if all the checks reported false as the
   * first thing they did (no race because active-ness not calculated until the
   * first one reports in, and at that point, 100% false)
   * - Stats check somewhat easier - only calculate targets from stats that have
   * something meaningful to say. Like the race above, wouldn't have to handle
   * the undefined target case, because nothing calculated until the first stats
   * reported. Otoh, filling it with 0s just means scale down, but we start at
   * the min so we wouldn't anyway */
  def makeInstances(ns: Iterable[Int]) = d match
  {
    case i: Instance =>
    {
      val cs = ns.map( n => (n -> context.actorOf(InstanceMonitor.props(i, n), s"instance-$n")) ).toMap
      val ls = cs.map { c => (c._2 -> false) }.toMap
      (cs, ls)
    }
    case g: Group =>
    {
      val cs = ns.map( n => (n -> context.actorOf(GroupMonitor.props(g, n), s"instance-$n")) ).toMap
      val ls = cs.map { c => (c._2 -> false) }.toMap
      (cs, ls)
    }
  }

  val (checks, livenesses) = makeInstances(Range(0, d.targetMin))
  val (stats, targets) = d match
  {
    case i: Instance =>
    {
      val ss = i.loadStats.map( s => (s -> context.actorOf(StatsAggregator.props(s), s"stats-$s")) ).toMap
      val ts = ss.map { s => (s._2 -> 0.0) }.toMap
      (ss, ts)
    }
    case g: Group =>
    {
      val ss = g.children.map( c => (c -> context.actorOf(StatsAggregator.props(c), s"stats-$c")) ).toMap
      val ts = ss.map { s => (s._2 -> 0.0) }.toMap
      (ss, ts)
    }
  }

  startWith(Inactive, Children(checks, stats, livenesses, targets))


  when(Inactive)
  {
    case Event(Liveness(ok), s: Children) =>
    {
      val newLives = s.livenesses.updated(sender, ok)
      val newState = s.copy(livenesses = newLives)

      if (newLives.values.count(ok => ok) >= d.activeMin)
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

      if (newLives.values.count(ok => ok) < d.activeMin)
        goto(Inactive) using newState
      else
        stay using newState
    }
  }
  
  when(Stopping)
  {
    case Event(Terminated(child), s: Children) =>
    {

    }
  }

  whenUnhandled
  {
    case Event(Stop, s: Children) =>
    {
      s.checks.values.foreach{ c => context.watch(c); c ! Stop }
      stop other children?
      goto(Stopping) // Above is a graceful stop message so we don't just declare ourself Inactive yet, but wait for that to propage up from the instances.
    }

    // TODO: This being here shows it should be in its own target-aggregator actor (see evernote)?
    case Event(ScaleTarget(target), s: Children) =>
    {
      val curTarget = s.targets.values.max.ceil.toInt
      val newTargets = s.targets.updated(sender, target)
      val maxTarget = newTargets.values.max.ceil.toInt
      val newTarget = Math.max(Math.min(maxTarget, d.targetMax), d.targetMin)
      val deltaTarget = newTarget - curTarget

      if (deltaTarget > 0)
      {
        val currentIds = s.checks.keySet
        val newIds = Range(d.targetMin, d.targetMax).filterNot(n => currentIds.contains(n)).take(Math.abs(deltaTarget))
        val (newChecks, newLivenesses) = makeInstances(newIds)

        stay using Children(s.checks ++ newChecks, s.stats, s.livenesses ++ newLivenesses, newTargets)
      }
      else if (deltaTarget < 0)
      {
        /* Picking the quietest instances would involve getting all (instance x
         * stat) pairs (not currently available at this level) and calculating
         * the averge normalised stats for each instance - perpendicular to what
         * we currently do. I assert this isn't neccessary because the rest of
         * the scaling i.e. scaling based on average indicators across instances
         * is predicated on instances in a cluster being able to load balance
         * well between themselves */
        val victimIds = s.checks.keySet.filterNot( n => n < d.targetMin ).take(Math.abs(deltaTarget))
        val victimCheckers = victimIds.map(s.checks)
        victimCheckers foreach { v => v ! Kill }

        stay using Children(s.checks -- victimIds, s.stats, s.livenesses -- victimCheckers, newTargets)
      }
      else
      {
        stay using s.copy(targets = newTargets)
      }

      //TODO report to whom? ! ScaleTarget(newTarget / d.max)
    }

    case Event(e, s) =>
      log.warning("received unhandled event {} in state {}/{}", e, stateName, s)
      stay
  }


  onTransition
  {
    /* Only matches the first one */
    case _ -> Active =>
    {
      context.system.eventStream.publish(UserEvent(s"Cluster Active ${d.name}"))
      context.parent ! Liveness(true)
    }
    case _ -> Inactive =>
    {
      context.system.eventStream.publish(UserEvent(s"Cluster Inactive ${d.name}"))
      context.parent ! Liveness(false)
    }
  }

  /* or...
   * onTransition(handler _)
   * def handler(from : State, to : State)
   */


  initialize
}
