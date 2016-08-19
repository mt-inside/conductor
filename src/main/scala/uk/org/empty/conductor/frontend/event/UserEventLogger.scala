/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.frontend.event

import akka.actor.{ Actor, Props }
 
// TODO: move
final case class UserEvent(msg: String)

object UserEventLogger
{
  def props = Props[UserEventLogger]
}

class UserEventLogger extends Actor
{
  def receive =
  {
    case UserEvent(msg) => println(msg)
  }
}
