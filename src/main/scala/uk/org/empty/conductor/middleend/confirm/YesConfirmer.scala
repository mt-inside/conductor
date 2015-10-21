package uk.org.empty.conductor.middleend.confirm

import akka.actor._

object YesConfirmer
{
  def props = Props[YesConfirmer]
}

class YesConfirmer extends Actor with ActorLogging
{
  import YesConfirmer._
  import ConfirmerProtocol._

  def receive =
  {
    case Confirm(target, msg, question) => target forward msg
  }
}
