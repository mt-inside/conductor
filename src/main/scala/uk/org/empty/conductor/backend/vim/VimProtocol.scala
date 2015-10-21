package uk.org.empty.conductor.backend.vim

import uk.org.empty.conductor.frontend.model.Instance
///
//////how have different implimentatiions? Trait defining messages that are sent and received is the equiv of an interface?
//////THis doesn't seem very OO to me. Actors are objects. we have an object modelling the vim (connection details etc).
//////kinda feels like we should have an RAII-like object representing the instnace?
//////- what would be the oerations? only stop?
//////- can still do this - call the vim actor, get an actor ref for the instance
///
object VimProtocol
{
  /* Messages received */
  final case class StartMachine(i: Instance, name: String) // name: name to give the IaaS instance
  final case class StopMachine(machineId: String)
  final case class AllowAddress(machineId: String, addr: String)
  final case class DisallowAddress(machineId: String, addr: String)
  final case class AttachVolume(machineId: String, vol: String)
  final case class DetatchVolume(machineId: String, vol: String)

  /* Messages sent */
  /* These don't need a machine id because they're returned to sender */
  final case class MachineStarted(id: String)
  final case object MachineStopped
  final case class AddressAllowed(addr: String)
  final case class AddressDisallowed(addr: String)
  final case class VolumeAttached(vol: String)
  final case class VolumeDetached(vol: String)
}
