package committee.nova.proton.core.event.impl

import committee.nova.proton.api.perm.IGroup
import committee.nova.proton.core.event.impl.ProtonGroupInitializationEvent.{getAllGroups, groupSet}
import cpw.mods.fml.common.eventhandler.Event

import scala.collection.mutable

object ProtonGroupInitializationEvent {
  private val groupSet = new mutable.HashSet[IGroup]()

  def getAllGroups: Iterator[IGroup] = groupSet.toIterator
}

class ProtonGroupInitializationEvent extends Event {
  def addGroup(groups: IGroup*): Unit = for (group <- groups) groupSet.add(group)

  def getGroups: Iterator[IGroup] = getAllGroups
}
