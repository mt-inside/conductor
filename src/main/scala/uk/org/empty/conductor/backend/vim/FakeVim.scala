package uk.org.empty.conductor.backend.vim

import akka.actor._

import uk.org.empty.conductor.frontend.model.Instance

object FakeVim
{
  def props = Props[FakeVim]
}

class FakeVim extends Actor with ActorLogging
{
  import FakeVim._
  import VimProtocol._

  def receive =
  {
    case StartMachine(i, name) => log.debug(s"Got StartMachine for $name, pretending MachineStarted"); sender ! MachineStarted(name)
    case e: StopMachine => log.debug(s"Got $e, pretending MachineStopped"); sender ! MachineStopped
  }
}
