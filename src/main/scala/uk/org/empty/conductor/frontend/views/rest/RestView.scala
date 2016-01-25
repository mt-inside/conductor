package uk.org.empty.conductor.frontend.view.rest

import akka.actor.{Actor, Props, ActorSelection}

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future

import spray.http._
import spray.routing._

import spray.httpx.Json4sSupport
import org.json4s.DefaultFormats
import spray.httpx.unmarshalling.FromRequestUnmarshaller
import spray.httpx.marshalling.ToResponseMarshaller


import uk.org.empty.conductor.frontend.view._
import uk.org.empty.conductor.frontend.model._
import uk.org.empty.conductor.frontend.controller._


/* HttpService trait brings route DSL and the requirement to set actorRefFactory
 * There is an HttpServiceActor which defines aRF but that would couple these
 * two concerns */
trait RestViewService extends HttpService with Json4sSupport
{
  // TODO: should probably use the actor system's ec
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(5.seconds)

  val json4sFormats = DefaultFormats

  // TODO: no auto JSON marshalling; don't always want it? OR return json all
  // the time (msg / id entry)
  def getStore(storeName: String) : ActorSelection
  def die


  /* Taking an implicit Manifest[T] seems to allow auto-generation of the unmarshaller, but not the marshaller */
  def crudRoutes[T](storeName: String)(implicit marshaller: ToResponseMarshaller[T], um: FromRequestUnmarshaller[T]) =
    (pathEnd & post) {
      entity(as[T]) { obj =>
        complete {
          (getStore(storeName) ? CrudMessages.Create[T](obj)).mapTo[CrudMessages.Created[T]].map
          {
            case CrudMessages.Created(obj: T) => obj
          }
        }
      }
    } ~
    (pathEnd & get) {
      complete {
        (getStore(storeName) ? CrudMessages.List).mapTo[CrudMessages.Listing[T]].map
        {
          case CrudMessages.Listing(objs: Iterable[T]) => objs
        }
      }
    } ~
    (path(Segment) & get) { name =>
      complete {
        (getStore(storeName) ? CrudMessages.Show(name)).mapTo[CrudMessages.Details[T]].map
        {
          case CrudMessages.Details(obj: T) => obj
        }
      }
    } ~
    (path(Segment) & put) { name =>
      entity(as[T]) { obj =>
        complete {
          (getStore(storeName) ? CrudMessages.Update[T](name, obj)).mapTo[CrudMessages.Updated[T]].map
          {
            case CrudMessages.Updated(name, obj: T) => s"Updated $name to be $obj"
          }
        }
      }
    } ~
    (path(Segment) & delete) { name =>
      complete {
        (getStore(storeName) ? CrudMessages.Delete(name)).mapTo[CrudMessages.Deleted].map
        {
          case CrudMessages.Deleted(name) => s"Deleted $name"
        }
      }
    }

  val routes =
    pathPrefix("v1") {
      // FIXME: because "Definition" store is shared between Instance and Group,
      // both list operations return both types. Fix is to extend store class
      // for Definition and add methods to get just one or the other? But how to
      // call from here? Maybe split the stores, and have controller logic that
      // knows to check both when looking for definitions (e.g. reading,
      // checking for dup names before adding, etc)
      pathPrefix("instance")   { crudRoutes[Instance]("Definition") } ~
      pathPrefix("group")      { crudRoutes[Group]("Definition") } ~
      pathPrefix("deployment") { crudRoutes[Deployment]("Deployment") }
    } ~
    pathPrefix("test") {
      (path("die") & post) {
        complete { die; "argh" }
      }
    }
}

object RestView
{
  def props = Props[RestView]
}

class RestView extends Actor with RestViewService
{
  def actorRefFactory = context

  def getStore(s: String) = context.actorSelection("/user/Stores/" + s)
  def die = context.system.shutdown

  /* Define the routes using spray-routing's DSL and let spray "run" them.
   * Seems to use this actor as a dispatcher and handle the actual requests on
   * workers. This is perfectly sensible and means the routes can blithley
   * block, e.g. using ask(). Otoh, routes' own actors are hidden, e.g. can't
   * tell() from a route, because the receive function isn't available to be
   * customised (hint: it's not this one).
   * The alternative is to forgo the DSL and write our own receive that manually
   * matches HttpEntity(...). */
  def receive = runRoute(routes)
}
