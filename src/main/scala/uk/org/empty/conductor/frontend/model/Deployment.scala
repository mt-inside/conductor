/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.frontend.model

case class Deployment
(
  name: String,

  definition: String,
  perInstanceData: Map[String, Map[String, String]] //path -> (key -> value)
) extends ModelId(name)
