/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.frontend.data

import akka.actor._

import uk.org.empty.conductor.frontend.model._
import uk.org.empty.conductor.frontend.controller.CrudMessages

/* TODO: these should both actually be traits, extneded by each store we want?
 * */
object GenStore
{
  def props[T <: ModelId] = Props[GenStore[T]]

  case object Watch
  case object Unwatch
}

class GenStore[T <: ModelId] extends Actor with ActorLogging
{
  import GenStore._
  import CrudMessages._

  def genStore(objs: Map[String, T], watchers: Set[ActorRef]) : Receive =
  {
    case Create(obj: T) =>
    {
      log.debug(s"Create for $obj")
      context.become(genStore(objs + (obj.name -> obj), watchers))
      watchers.foreach(_ ! Created(obj))
      sender ! Created(obj)
    }
    case List => sender ! Listing(objs.values)
    case Show(name: String) => sender ! Details(objs(name))
    case Update(name: String, obj: T) => sender ! Details(objs(name))
    case Delete(name: String) =>
    {
      context.become(genStore(objs - name, watchers))
      watchers.foreach(_ ! Deleted(name))
      sender ! Deleted(name)
    }

    case Watch => context.become(genStore(objs, watchers + sender))
    case Unwatch => context.become(genStore(objs, watchers - sender))
  }

  def receive = genStore(Map(), Set())
}
