package uk.org.empty.conductor.frontend.model

case class Group
(
  name: String,

  activeMin: Int,
  targetMin: Int,
  targetMax: Int,
  target: String, // n: Int or Auto
  placementPolicy: String,

  children: Seq[String],
  dependencies: Map[String, String],
  restartStyle: String // none, dependents, all
) extends Definition(name, activeMin, targetMin, targetMax, target, placementPolicy)
