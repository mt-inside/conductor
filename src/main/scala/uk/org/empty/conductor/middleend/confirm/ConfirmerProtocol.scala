package uk.org.empty.conductor.middleend.confirm

import akka.actor.ActorRef

object ConfirmerProtocol
{
  final case class Confirm(target: ActorRef, msg: Any, question: String)
}
