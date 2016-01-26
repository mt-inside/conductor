package uk.org.empty.conductor

import scala.util.Try
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor._
import akka.pattern.ask
import akka.io.IO
import spray.can.Http
import com.typesafe.config._
import com.typesafe.scalalogging._

/* TODO: frontend -> usermodel? (alongside operationalmode, vimmodel?) */
import frontend.controller._
import frontend.data._
import frontend.model._
import frontend.view.rest._
import frontend.event._
import middleend.Deployer
import middleend.confirm._
import backend.vim._

object Main extends App with LazyLogging
{
  val conf = ConfigFactory.load

  implicit val system = ActorSystem("conductor")
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(5.seconds)

  /* TODO: is actor creation sync? If not, each needs a ready message, and we
   * need a receive state machine thing that waits for each one in turn */

  /* == FRONT END == */

  /* Data Stores */
  val stores = system.actorOf(StoreManager.props, "Stores")

  /* JSON/REST interface */
  val restView = system.actorOf(RestView.props, "RestView")
  val rHost = Try(conf.getString("restApi.host")).getOrElse("0.0.0.0")
  val rPort = Try(conf.getInt("restApi.port")).getOrElse(1337)
  (IO(Http) ? Http.Bind(restView, rHost, rPort))
    .mapTo[Http.Event]
    .map {
      case Http.Bound(addr) =>
        logger.info(s"REST interface bound to $addr")
      case Http.CommandFailed(cmd) =>
        logger.error(s"REST interface could not bind to $rHost:$rPort; ${cmd.failureMessage}")
        system.terminate
    }

  //TODO: factor out into confirmer factory method which will return this or
  //the always-true confirmer, or one with a different transport, etc
  /* Long-polling confirmer */
  val confirmer = system.actorOf(YesConfirmer.props, "Confirmer")
  /*val confirmer = system.actorOf(Confirmer.props(stores), "Confirmer")
  val cHost = Try(conf.getString("confirmer.host")).getOrElse("0.0.0.0")
  val cPort = Try(conf.getInt("confirmer.port")).getOrElse(1338)
  (IO(Http) ? Http.Bind(confirmer, cHost, cPort))
    .mapTo[Http.Event]
    .map {
      case Http.Bound(addr) =>
        logger.info(s"Confirmer bound to $addr")
      case Http.CommandFailed(cmd) =>
        logger.error(s"Confirmer could not bind to $cHost:$cPort; ${cmd.failureMessage}")
        system.shutdown
    }*/

  val uel = system.actorOf(UserEventLogger.props)
  system.eventStream.subscribe(uel, classOf[UserEvent])


  /* == MIDDLE END == */

  val deployer = system.actorOf(Deployer.props, "Deployer")


  /* == BACK END == */

  val vim = system.actorOf(FakeVim.props, "Vim")
}
