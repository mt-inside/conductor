package uk.org.empty.conductor.frontend.model

case class Instance
(
  name: String,

  activeMin: Int,
  targetMin: Int,
  targetMax: Int,
  target: String, // n: Int or Auto
  placementPolicy: String, // colocate, distribute

  image: String,
  flavour: String,
  networks: Seq[String],
  recoverType: String, // restart, replace
  replaceTiming: String, // parallel, serial
  quarantine: Boolean, // yes, no
  bootTimeMax: Int,
  livenessChecks: Seq[String],
  busyChecks: Seq[String], // empty or missing implies no quesce state
  loadStats: Seq[String],
  loadTargets: Seq[Double]
) extends Definition(name, activeMin, targetMin, targetMax, target, placementPolicy)
