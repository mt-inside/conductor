package uk.org.empty.conductor.frontend.model

abstract class Definition
(
  name: String,

  activeMin: Int,
  targetMin: Int,
  targetMax: Int,
  target: String, // n: Int or Auto
  placementPolicy: String
) extends ModelId(name)
{
  def activeMin: Int
  def targetMin: Int
  def targetMax: Int
  def target: String
  def placementPolicy: String
}
