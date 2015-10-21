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
 */
object InstanceMonitor
{
  /* Props */
  def props(i: Instance, instanceId: Int) = Props(classOf[InstanceMonitor], i, instanceId)

  /* Received and Sent events */
  // MonitorProtocol._

  /* States */
  sealed trait State
  case object NotRequested extends State
  case object NotRunning extends State
  case object Running extends State
  case object Active extends State
  case object Maintanence extends State
  case object Quiescent extends State
  case object Quarentine extends State
  case object Terminated extends State

  /* Data */
  sealed trait Data
  /* Should really have a NoMachine class for the inital state but cba with the C/P */
  final case class Machine(
    id: String, /* Don't use UUID so as to be IaaS/VIM-agnostic */
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

  /* Deps */
  val vim = context.actorSelection("/user/Vim")

  /* Start the liveness checkers and load fetchers */
  val checks = i.livenessChecks.map { c => (c -> context.actorOf(LivenessChecker.props(c), s"liveness-$c")) }.toMap
  val stats = i.loadStats.map { s => (s -> context.actorOf(StatFetcher.props(s), s"stat-$s")) }.toMap

  /* Populate initial set of liveness values */
  val livenesses = checks.map { c => (c._2 -> false) }.toMap
  // Fetchers do not report into this actor

  startWith(NotRequested, Machine(null, checks, stats, livenesses))


  when(NotRequested, stateTimeout = 1.second)
  {
    case Event(StateTimeout, _) => goto(NotRunning)
  }

  when(NotRunning)
  {
    case Event(VimProtocol.MachineStarted(newId), m: Machine) =>
    {
      goto(Running) using m.copy(id=newId)
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
      vim ! VimProtocol.StopMachine(m.id)

      goto(NotRunning) using m.copy(id = null)
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
        vim ! VimProtocol.StopMachine(m.id)
        // TODO: add support for "wait for termination" feature: new state
        // "waitfortermination" and go to that here. in that state, expect
        // MachineStopped and then start it. *really* looke like starting needs
        // factoring to a function, if not a state.
        goto(NotRunning) using m.copy(id = null)
        ///git hash, build time in resource, print on start up
        //add vim ! RestartInPlace & goto running
        ///could transition into a NotRequested state and have the transition //into that start the vm (startWith does not show a transition into the //start state)
      }
    }
  }


  onTransition
  {
    /* Only matches the first one */
    case _ -> NotRunning =>
    {
      context.system.eventStream.publish(UserEvent(s"Instance NotRunning ${i.name}"))

      /* Start the instance */
      vim ! VimProtocol.StartMachine(i, i.name + "-" + instanceId)
    }
    case _ -> Running =>
    {
      context.system.eventStream.publish(UserEvent(s"Instance Running ${i.name}"))
    }
    case _ -> Active =>
    {
      context.system.eventStream.publish(UserEvent(s"Instance Active ${i.name}"))
    }
  }


  initialize
}
