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
