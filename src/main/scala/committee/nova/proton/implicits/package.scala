package committee.nova.proton

import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.perm.PermNode
import committee.nova.proton.core.player.storage.ProtonEEP
import committee.nova.proton.core.server.storage.ProtonSavedData
import net.minecraft.entity.player.EntityPlayer

package object implicits {
  implicit class PlayerImplicit(val player: EntityPlayer) {
    def getProton: ProtonEEP = player.getExtendedProperties(ProtonEEP.id).asInstanceOf[ProtonEEP]

    def wasInitialized: Boolean = getProton.wasInitialized

    def setInitialized(initialized: Boolean): Unit = getProton.setInitialized(initialized)

    def getPerms: Array[IPermNode] = getProton.getPerms

    def hasPerm(node: IPermNode): Boolean = getProton.hasPermission(node)

    def hasPerm(node: String): Boolean = getProton.hasPermission(node)

    def addPerm(node: IPermNode): Boolean = ProtonSavedData.get.getPermNodes.contains(node) && getProton.addPerm(node)

    def addPerm(node: String): Boolean = addPerm(PermNode(node))

    def removePerm(node: IPermNode): Boolean = getProton.removePerm(node)

    def removePerm(node: String): Boolean = removePerm(PermNode(node))

    def getGroups: Array[IGroup] = getProton.getGroups

    def addToGroup(group: IGroup): Boolean = ProtonSavedData.get.getGroups.contains(group) && getProton.addToGroup(group)

    def addToGroup(group: String): Boolean = {
      ProtonSavedData.get.getGroup(group) match {
        case Some(g) => getProton.addToGroup(g)
        case None => false
      }
    }

    def removeFromGroup(group: IGroup): Boolean = getProton.removeFromGroup(group)

    def removeFromGroup(group: String): Boolean = {
      getProton.getGroups.find(g => group.equals(g.getName)) match {
        case Some(g) => getProton.removeFromGroup(g)
        case None => false
      }
    }

    def dissociate(group: IGroup): Boolean = getProton.dissociate(group)

    def dissociate(group: String): Boolean = {
      getProton.getGroups.find(g => group.equals(g.getName)) match {
        case Some(g) => getProton.dissociate(g)
        case None => false
      }
    }
  }
}
