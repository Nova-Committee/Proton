package committee.nova.proton.core.event.impl

import committee.nova.proton.core.event.impl.ProtonPlayerInitializationEvent.{funcs, getAllFuncs}
import cpw.mods.fml.common.eventhandler.Event
import net.minecraft.entity.player.EntityPlayerMP

import scala.collection.mutable

object ProtonPlayerInitializationEvent {
  private val funcs = new mutable.MutableList[EntityPlayerMP => Unit]()

  def getAllFuncs: List[EntityPlayerMP => Unit] = funcs.toList
}

class ProtonPlayerInitializationEvent extends Event {
  def getFuncs: List[EntityPlayerMP => Unit] = getAllFuncs

  def addFunc(func: EntityPlayerMP => Unit): Unit = funcs.+=(func)
}
