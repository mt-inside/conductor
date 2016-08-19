/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.frontend.controller

sealed trait CrudAction
sealed trait CrudAnswer

object CrudMessages
{
  /* Requests / Verbs */
  case class Create[T](obj: T) extends CrudAction
  case object List extends CrudAction
  case class Show(name: String) extends CrudAction
  case class Update[T](name: String, obj: T) extends CrudAction
  case class Delete(name: String) extends CrudAction

  /* Responses / Nouns */
  case class Created[T](obj: T) extends CrudAnswer
  case class Listing[T](objs: Iterable[T]) extends CrudAnswer
  case class Details[T](obj: T) extends CrudAnswer
  case class Updated[T](name: String, obj: T) extends CrudAnswer
  case class Deleted(name: String) extends CrudAnswer

  case class Error(reason: String) extends CrudAnswer
}
