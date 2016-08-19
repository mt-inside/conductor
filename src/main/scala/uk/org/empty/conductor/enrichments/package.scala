/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor

package object enrichments
{
  implicit class RichIterable[T: Numeric](val xs: Iterable[T])
  {
    def average = implicitly[Numeric[T]].toDouble(xs.sum) / xs.size
  }
}
