package uk.org.empty.conductor.frontend.model

case class Instance
(
  name: String,

  activeMin: Int,
  targetMin: Int,
  targetMax: Int,
  target: String, // n: Int or Auto
  placementPolicy: String,

  image: String,
  flavour: String,
  networks: Seq[String],
  recoverType: String, // replace, replace
  replaceTiming: String, // parallel, serial
  quarantine: Boolean,
  bootTimeMax: Int,
  livenessChecks: Seq[String],
  loadStats: Seq[String],
  loadTargets: Seq[Double]
) extends Definition(name, activeMin, targetMin, targetMax, target, placementPolicy)
