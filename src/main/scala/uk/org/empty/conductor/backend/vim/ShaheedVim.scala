package uk.org.empty.conductor.backend.vim

///import spray.client?
import akka.actor._

import uk.org.empty.conductor.frontend.model.Instance


object ShaheedVim
{
  def props = Props[ShaheedVim]
}

class ShaheedVim extends Actor with ActorLogging
{
  import ShaheedVim._
  import VimProtocol._

  def receive =
  {
    case _ => log.error("wtf")
///    /* I/O like this is generally taken to be risky, thus worthy of delgation to
///     * workers. However we're not exactly making direct socket calls;
///     * spray-client looks safe enough.
///     * If the VIM isn't re-entrant, this actor is the ideal place to serialise
///     * calls by blocking on the futures. */
///    case StartMachine(i) =>
///    {
///      val client = sender
///      val response = Post(url, FormData("foo")).sendReceive
///      response onComplete
///      {
///        case Success(HttpResponse(status, body, headers, protocol)) =>
///          log.info("ShaheedVim call successful")
///          client ! MachineStarted(name, uuid)
///        case Failure(e) =>
///          log.warn(s"ShaheedVim call error: ${e}")
///      }
///    }
///    case StopMachine(name) =>
///    {
///      val client = sender
///      val response = Post(url, FormData("foo")).sendReceive
///      response onComplete
///      {
///        case Success(HttpResponse(status, body, headers, protocol)) =>
///          log.info("ShaheedVim call successful")
///          client ! MachineStopped(name)
///        case Failure(e) =>
///          log.warn(s"ShaheedVim call error: ${e}")
///      }
///    }
  }
}
