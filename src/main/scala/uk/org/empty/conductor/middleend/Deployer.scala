package uk.org.empty.conductor.middleend

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import uk.org.empty.conductor.frontend.controller.CrudMessages
import uk.org.empty.conductor.frontend.data.GenStore
import uk.org.empty.conductor.frontend.model.{Deployment, Definition}
import uk.org.empty.conductor.middleend.monitor.Cluster

/* TODO:
 * - will surely get Liveness and Target messages from child clusters as they
 * don't know they're not top level. Just drop these? Should probably raise a
 * messahe for Target that would cause a scale saying it's reached the top of
 * the stack and can't */
object Deployer
{
  /* Props */
  def props = Props[Deployer]

  /* Received */
  final case class Deploy(d: Deployment)

  /* Sent */
}

class Deployer extends Actor with ActorLogging
{
  import Deployer._

  val depS = context.actorSelection("/user/Stores/Deployment")
  depS ! GenStore.Watch
  val defS = context.actorSelection("/user/Stores/Definition")

  def receive =
  {
    /* Ah, so this is the "compile" stage.
     * E.g. note how this class has to query several stores. This is kinda OK in
     * the "compiler", but don't want downstream monitor classes to have to do
     * this, so this thing should create an object with links to other objects,
     * not just keys (or maybe one big flat object?). Currently this has to
     * happen in:
     * - GroupMonitor*/
    case CrudMessages.Created(d: Deployment) =>
    {
      import context.dispatcher
      implicit val timeout = Timeout(10.seconds)

      log.info("Deployment database changed!")

      (defS ? CrudMessages.Show(d.definition))
        .mapTo[CrudMessages.Details[Definition]]
        .map { d => context.actorOf(Cluster.props(d.obj), s"cluster-${d.obj.name}") }
        .recover { case ex => log.warning(s"Can't deploy $d") }
    }

    case CrudMessages.Deleted(name) =>
    {
      // TODO: should almost certainly be a gracefulStop(). Timeout may want to
      // be very long because we will but VMs in Quiesced, and that may have
      // custom / no timeout.
      // TODO: in fact, probably shouldnt use gracefullStop at all, rather just
      // message pass, and either have the actors kill themselves eventually or
      // maybe hang around forever (in terminated, quarentine etc). Don't allow
      // re-use of deployment names and all is dandy
      // TDDO: the fact this is an actorFor (can;t stop an actorSeletion) really
      // indicates that we should have a map of child names to actors - this
      // will be a recurring pattern down the hierarchy.
      context.stop(context.actorFor(s"cluster-${name}"))
    }

    case _ => log.error("wft")
  }
}
