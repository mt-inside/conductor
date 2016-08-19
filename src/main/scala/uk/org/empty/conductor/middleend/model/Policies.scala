/*
 * Copyright (C) 2015-2016 Matthew Turner.
 *
 * All rights reserved.
 */
package uk.org.empty.conductor.frontend.model

/* TODO: proper way to do enums these days? */
object PlacementPolicy extends Enumeration
{
  type PlacementPolicy = Value
  val Colocated = Value("Colocated")
  val Dislocated = Value("Dislocated")
  val Anywhere = Value("Anywhere")
}

object KillPolicy extends Enumeration
{
  type KillPolicy = Value
  val None = Value("Kill None")
  val Dependents = Value("Kill Dependents")
  val All = Value("Kill All")
}

object InstanceRecoveryPolicy extends Enumeration
{
  type InstanceRecoveryPolicy = Value
  val Replace = Value("Replace")
  val Restart = Value("Restart")
}

object InstanceReplacementPolicy extends Enumeration
{
  type InstanceReplacementPolicy = Value
  val Parallel = Value("Parallel")
  val Serial = Value("Serial")
}

object InstanceQuarentinePolicy extends Enumeration
{
  type InstanceQuarentinePolicy = Value
  val Quarentine = Value("Quarentine")
  val NoQuarentine = Value("No Quarentine")
}
