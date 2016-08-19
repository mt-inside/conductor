/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.frontend.data

import akka.actor._

import uk.org.empty.conductor.frontend.model._


object StoreManager
{
  def props = Props[StoreManager]
}

class StoreManager extends Actor with ActorLogging
{
  import StoreManager._

  /* TODO: nasty. DI? One controller? */
  context.actorOf(GenStore.props[Deployment], "Deployment")
  context.actorOf(GenStore.props[Definition], "Definition")

  def receive =
  {
    case _ => log.error("wtf?")
  }
}
