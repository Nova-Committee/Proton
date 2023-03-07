package committee.nova.proton.core.event.impl

import committee.nova.proton.api.perm.IGroup
import committee.nova.proton.core.event.impl.ProtonImmutableGroupInitializationEvent.{getImmutableGroups, immutableGroups}
import net.minecraftforge.fml.common.eventhandler.Event

import scala.collection.mutable

object ProtonImmutableGroupInitializationEvent {
  private val immutableGroups = new mutable.HashSet[IGroup]()

  def getImmutableGroups: Array[IGroup] = immutableGroups.toArray
}

class ProtonImmutableGroupInitializationEvent extends Event {
  def addGroup(groups: IGroup*): Unit = {
    for (group <- groups) {
      group.setImmutable(true)
      immutableGroups.add(group)
    }
  }

  def getGroups: Array[IGroup] = getImmutableGroups
}
