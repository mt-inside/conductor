package uk.org.empty.conductor.middleend.monitor

import akka.actor._
import scala.concurrent.duration._

import uk.org.empty.conductor.frontend.model._
import uk.org.empty.conductor.frontend.event.UserEvent
import uk.org.empty.conductor.backend.vim.VimProtocol

/*
 * TODO: restart-ability. This individual actor might be restarted due to a
 * crash, or the whole system may be restarted. Either way, it should "reattach"
 * to the running instance.
 * Actor restart:
 * - can call startWith & initialise from preStart and postRestart instead of
 * ctor. Do that.
 * - postRestart version to put into state reattaching, which should wait until
 * all checkers have reported in? (they'll be started anew too so they should
 * tick over to active pretty quick). This seems nasty
 * - rather, should probably factor targets and livenesses out and have this
 * thing's state be just: id,alive? Persist that and pick it back up when we
 * restart.
 *
 * TODO: impliment the Instance model flags e.g. restart serial/parallel (might
 * reuse Sroping state with an intent field in the data to tell it what to do
 * when the machine stops - either kill self or back to starting. Or probably
 * have another state to make it more obiouvlss
 */
object InstanceMonitor
{
  /* Props */
  def props(i: Instance, instanceId: Int) = Props(classOf[InstanceMonitor], i, instanceId)

  /* Received and Sent events */
  // MonitorProtocol._
  // - final case class ScaleTarget(instances: Double)
  // - final case class Liveness(ok: Boolean)
  // - final case object Stop
  // - final case object Stopped
  // VimProtocol._
  // - final case class MachineStarted(id: String)
  // - final case class MachineStopped(id: String)

  /* States */
  sealed trait State
  case object NotRequested extends State
  case object Starting extends State
  case object Running extends State
  case object Active extends State
  case object Maintanence extends State
  case object Quiescing extends State
  case object Stopping extends State

  /* Data */
  sealed trait Data
  /* Should really have a NoMachine class for the inital state but cba with the C/P
   * TODO: but seriously, do, because we're just calling Option::get, which is
   * basically using a null */
  final case class Machine(
    id: Option[String], /* Don't use UUID so as to be IaaS/VIM-agnostic */
    /* Own */
    checks: Map[String, ActorRef], // Liveness Checkers
    stats: Map[String, ActorRef],
    /* Receive */
    livenesses: Map[ActorRef, Boolean]
    // Individual instances spawn stat fetchers but don't receive the load info.
  ) extends Data
}

class InstanceMonitor(i: Instance, instanceId: Int) extends LoggingFSM[InstanceMonitor.State, InstanceMonitor.Data]
{
  import InstanceMonitor._
  import MonitorProtocol._

  val instanceName = i.name + "-" + instanceId

  /* Deps */
  val vim = context.actorSelection("/user/Vim")

  /* Start the liveness checkers and load fetchers */
  val checks = i.livenessChecks.map { c => (c -> context.actorOf(LivenessChecker.props(c), s"liveness-$c")) }.toMap
  val stats = i.loadStats.map { s => (s -> context.actorOf(StatFetcher.props(s), s"stat-$s")) }.toMap

  /* Populate initial set of liveness values */
  val livenesses = checks.map { c => (c._2 -> false) }.toMap
  // Fetchers do not report into this actor

  startWith(NotRequested, Machine(None, checks, stats, livenesses))


  when(NotRequested, stateTimeout = 1.second)
  {
    case Event(StateTimeout, _) => goto(Starting)
  }

  when(Starting)
  {
    case Event(VimProtocol.MachineStarted(newId), m: Machine) =>
    {
      log.info(s"Instance ${instanceName} running with VIM ID ${newId}")
      goto(Running) using m.copy(id=Some(newId))
    }

    // aka failed to start
    case Event(VimProtocol.MachineStopped, m: Machine) =>
    {
      // Will trigger event and thus try again
      goto(Starting)
    }
  }

  when(Running, stateTimeout = Duration(i.bootTimeMax, SECONDS))
  {
    case Event(Liveness(ok), m: Machine) =>
    {
      val newLives = m.livenesses.updated(sender, ok)
      val newState = m.copy(livenesses=newLives)

      if (newLives.forall(t => t._2))
      {
        goto(Active) using newState
      }
      else
      {
        stay using newState
      }
    }
    case Event(StateTimeout, m: Machine) =>
    {
      /* Stop the instance that failed to come up */
      vim ! VimProtocol.StopMachine(m.id.get)

      goto(Starting) using m.copy(id = None)
    }
  }

  when(Active)
  {
    case Event(Liveness(ok), m: Machine) =>
    {
      val newLives = m.livenesses.updated(sender, ok)
      val newState = m.copy(livenesses=newLives)

      if (newLives.forall(t => t._2))
      {
        stay using newState
      }
      else
      {
        vim ! VimProtocol.StopMachine(m.id.get)
        // TODO: add support for "wait for termination" feature: new state
        // "waitfortermination" and go to that here. in that state, expect
        // MachineStopped and then start it. *really* looke like starting needs
        // factoring to a function, if not a state.
        goto(Starting) using m.copy(id = None)
        ///git hash, build time in resource, print on start up
        //add vim ! RestartInPlace & goto running
        ///could transition into a NotRequested state and have the transition //into that start the vm (startWith does not show a transition into the //start state)
      }
    }
  }

  when(Maintanence)
  {
    case _ => stay
  }

  when(Quiescing)
  {
    case _ => stay
  }

  when(Stopping)
  {
    case Event(VimProtocol.MachineStopped, m: Machine) =>
    {
      context.parent ! Stopped
      // Kill self
      stop(FSM.Normal)
    }
  }


  whenUnhandled
  {
    // Can happen in any state
    case Event(Stop, m: Machine) =>
    {
      // TODO can't assume we have an ID here (may still be starting, or even
      // NotRequested. VimDriver has to be happy to accept StopMachine in any
      // state, even when it's still starting it.
      vim ! VimProtocol.StopMachine(m.id.get)
      stop other children?
      goto(Stopping)
    }
  }


  /* Unlike the convenience method that takes a pfn but only runs the first
   * match, this way of registering transition handlers allows us to handle
   * multiple overlapping cases. */

  onTransition(loggingHandler _)
  def loggingHandler(from: InstanceMonitor.State, to: InstanceMonitor.State) =
  {
    if(to == Starting)
    {
      context.system.eventStream.publish(UserEvent(s"Instance Starting ${instanceName}"))
    }
    else if(to == Running)
    {
      context.system.eventStream.publish(UserEvent(s"Instance Running ${instanceName}"))
    }
    else if (to == Active)
    {
      context.system.eventStream.publish(UserEvent(s"Instance Active ${instanceName}"))
    }
  }

  onTransition(startingHandler _)
  def startingHandler(from: InstanceMonitor.State, to: InstanceMonitor.State) =
  {
    if (to == Starting)
    {
      /* Start the instance */
      vim ! VimProtocol.StartMachine(i, instanceName)
      // special "starting" (i.e. VIM thinking about it) state? Doesn't seem
      // necessary.
    }
  }

  onTransition(livenessHandler _)
  def livenessHandler(from: InstanceMonitor.State, to: InstanceMonitor.State) =
  {
    if (to == Active)
    {
      context.parent ! Liveness(true)
    }
    if (from == Active)
    {
      context.parent ! Liveness(false)
    }
  }


  initialize
}
