/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.backend.vim

import akka.actor._

import spray.json._
import DefaultJsonProtocol._

import uk.org.empty.conductor.frontend.model.Instance

case class VnfdInstance(
  image: String,
  flavour: String
)

object VnfdJsonProtocol extends DefaultJsonProtocol
{
  implicit val vnfdInstanceProtocol = jsonFormat2(VnfdInstance)
}
import VnfdJsonProtocol._


// FIXME Fuck it, render HOT, call Docker API (JSON over a socket?)

object EtsiVim
{
  def props = Props[EtsiVim]
}

class EtsiVim extends Actor with ActorLogging
{
  import EtsiVim._
  import VimProtocol._

  def receive =
  {
    case StartMachine(i, name) =>
    {
      val vd = VnfdInstance(i.image, i.flavour)
      log.error(vd.toJson.compactPrint) // .prettyPrint
      
      sender ! MachineStarted(name)
    }
    case e: StopMachine =>
    {
      sender ! MachineStopped
    }
  }
}
