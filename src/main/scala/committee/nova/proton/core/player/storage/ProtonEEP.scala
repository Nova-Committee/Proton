package committee.nova.proton.core.player.storage

import committee.nova.proton.Proton
import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.perm.PermNode
import committee.nova.proton.core.server.storage.ProtonSavedData
import committee.nova.proton.util.L10nUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.world.World
import net.minecraftforge.common.IExtendedEntityProperties

import java.util.UUID
import scala.collection.mutable

object ProtonEEP {
  final val id = "proton"
}

class ProtonEEP extends IExtendedEntityProperties {
  private var changingUUID: UUID = UUID.randomUUID()
  private var player: EntityPlayer = _
  private var world: World = _
  private var initialized = false
  private val groups: mutable.HashSet[IGroup] = new mutable.HashSet[IGroup]()
  private val permNodes: mutable.HashSet[IPermNode] = new mutable.HashSet[IPermNode]()

  override def saveNBTData(tag: NBTTagCompound): Unit = {
    val g = getGroups
    if (g.nonEmpty) {
      val groupsTag = new NBTTagList
      for (group <- g) groupsTag.appendTag(new NBTTagString(group.getName))
      tag.setTag("protonGroups", groupsTag)
    }
    if (permNodes.nonEmpty) {
      val nodesTag = new NBTTagList
      for (node <- permNodes) nodesTag.appendTag(new NBTTagString(node.getName))
      tag.setTag("protonNodes", nodesTag)
    }
    tag.setBoolean("protonInitialized", initialized)
  }

  override def loadNBTData(tag: NBTTagCompound): Unit = {
    groups.clear()
    permNodes.clear()
    initialized = tag.getBoolean("protonInitialized")
    if (tag.hasKey("protonGroups")) {
      val groupsTag = tag.getTagList("protonGroups", 8)
      for (i <- 0 until groupsTag.tagCount()) {
        val group = groupsTag.getStringTagAt(i)
        ProtonSavedData.get.getGroup(group) match {
          case Some(g) => groups.add(g)
          case None =>
        }
      }
    }
    if (tag.hasKey("protonNodes")) {
      val nodesTag = tag.getTagList("protonNodes", 8)
      for (i <- 0 until nodesTag.tagCount()) permNodes.add(PermNode(nodesTag.getStringTagAt(i)))
    }
  }

  override def init(entity: Entity, world: World): Unit = {
    if (!entity.isInstanceOf[EntityPlayer]) {
      Proton.LOGGER.error(L10nUtils.getFromCurrentLang("msg.proton.err.eepInit"))
      return
    }
    this.player = entity.asInstanceOf[EntityPlayer]
    this.world = world
  }

  def wasInitialized: Boolean = initialized

  def setInitialized(initialized: Boolean): Unit = this.initialized = initialized

  def hasPerm(node: IPermNode): Boolean = {
    if (permNodes.contains(node)) return true
    for (group <- getGroups) if (group.hasPerm(node)) return true
    false
  }

  def hasPerm(node: String): Boolean = {
    if (permNodes.exists(p => node.equals(p.getName))) return true
    for (group <- getGroups) if (group.hasPerm(node)) return true
    false
  }

  def checkGroups(): Unit = {
    val d = ProtonSavedData.get.getChangingUUID
    if (changingUUID.equals(d)) return
    for (group <- groups) if (!ProtonSavedData.get.getGroups.contains(group)) groups.remove(group)
    changingUUID = d
  }

  def getPerms: Array[IPermNode] = permNodes.toArray

  def getGroups: Array[IGroup] = {
    checkGroups()
    groups.toArray
  }

  def addPerm(perm: IPermNode): Boolean = permNodes.add(perm)

  def removePerm(perm: IPermNode): Boolean = permNodes.remove(perm)

  def addToGroup(group: IGroup): Boolean = groups.add(group)

  def removeFromGroup(group: IGroup): Boolean = groups.remove(group)

  def dissociate(group: IGroup): Boolean = {
    permNodes.++=(group.getPerms)
    removeFromGroup(group)
  }
}
