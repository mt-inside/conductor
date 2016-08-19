/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.backend.vim

import akka.actor._
import scala.concurrent.Future

import spray.http._
import spray.json.{JsonFormat, DefaultJsonProtocol}
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport

import uk.org.empty.conductor.frontend.model.Instance


case class Container(
  hostname: String,
  allocateTty: Boolean,
  openStdin: Boolean,
  cmd: String,
  entrypoint: String,
  image: String,
  labels: Map[String, String]
)
case class ContainerCreated(
  Id: String, // JSON library is sensitive to key case
  Warnings: List[String]
)

object DockerJsonProtocol extends DefaultJsonProtocol
{
  implicit val containerFormat = jsonFormat7(Container)
  implicit val containerCreatedFormat = jsonFormat2(ContainerCreated)
}



object DockerVim
{
  def props(host: String) = Props(classOf[DockerVim], host)
}

class DockerVim(host: String) extends Actor with ActorLogging
{
  import DockerVim._
  import VimProtocol._
  import SprayJsonSupport._ // Implicits for un/marshalling
  import DockerJsonProtocol._
  import context.dispatcher

  def receive =
  {
    case StartMachine(i, name) =>
    {
      val c = Container(name, false, false, "", "", i.image, Map("taggedby"->"matt"))

      /* TODO I/O like this is risky. Even though it doesn't block, it should be
       * delegated to an ephmeral worker (which can be constructed over sender
       * so not have to stash it, can can pipe the future to itself and receive
       * Success/Failure then message it's requester parameter (rememver it;s
       * not thread-safe to alter any actor state from the future callbacks,
       * which is why it's so common to pipe the future to self). */
      val createContainer: HttpRequest => Future[ContainerCreated] =
        addHeader("Host", host) ~>
      // TLS
        sendReceive ~>
        unmarshal[ContainerCreated]

      val response: Future[ContainerCreated] = createContainer(Post("/v1.21/containers/create", c))

      val requester = sender
      response.onSuccess
      {
        case cc => requester ! MachineStarted(cc.Id)
      }
      response.onFailure
      {
        case t =>
        {
          log.warning(s"Machine ${name} failed to start: " + t)
          requester ! MachineStopped //Special "died / failed to start" message type?
        }
      }
    }

    case StopMachine(id) =>
    {
      val deleteContainer: HttpRequest => Future[HttpResponse] =
        addHeader("Host", host) ~>
        sendReceive

      val response: Future[HttpResponse] = deleteContainer(Post(s"/v1.21/containers/${id}/kill"))

      val requester = sender
      response.onSuccess
      {
        case resp => requester ! MachineStopped(id)
      }
      response.onFailure
      {
        case t =>
        {
          log.warning(s"Machine ${id} failed to stop: " + t)
        }
      }
    }
  }
}
