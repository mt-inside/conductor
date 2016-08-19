/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.middleend.confirm

import akka.actor._


object HttpConfirmer
{
  def props = Props[HttpConfirmer]
}

class HttpConfirmer extends Actor with ActorLogging
{
  // TODO: trait for the actor's dispatcher as implicit ec (exists or write)
  import HttpConfirmer._
  import ConfirmerProtocol._

  def receive =
  {
    case _ => log.error("wtf")
///    /* This is a simple example. The question / response might be able to be
///     * couched as a future (i mean, everything ultimately can by ask()ing
///     * another actor...). If there's an actual blocking call then maybe best to
///     * spawn a worker actor for each one and let them block on it. Especially as
///     * that call will do network IO and be risky.
///     * However more likely is that question answer are done by message passing,
///     * e.g. send a message to an implimentation actor that punts it down a
///     * long-poll and sends a message back when there's an answer to a "webhook"
///     * return URI. In that case, just receive the reply message here */
///    case Confirm(target, msg, question) =>
///    {
///      log.info(s"Seeking confirmation for question ${question} between ${sender} and ${target}")
///      //timeout?
///      val response = Future(true)
///      response onComplete
///      {
///        case Success(true) => log.info("User said yes"); target forward msg
///        case Success(false) => log.info("User said no")
///        //case Failure(e: TimeoutException) => log.info("User didn't reply in time; not proceeding")
///        case Failure(e) => log.info(s"Failure getting user answer; not proceeding. Reason: ${ex}")
///      }
///    }
///        case HttpEntity(?? polling request) =>
///          block somehow until a confirm comes in, then send the question
///        case HttpEntiru(?? webhook reply) =>
///          target forward msg
  }
}
