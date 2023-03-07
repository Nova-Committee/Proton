package committee.nova.proton.core.player.storage.impl

import committee.nova.proton.Proton.protonCapability
import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.perm.PermNode
import committee.nova.proton.core.player.storage.api.IProtonCapability
import committee.nova.proton.core.server.storage.ProtonSavedData
import net.minecraft.nbt.{NBTBase, NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.{Capability, ICapabilitySerializable}

import java.util.UUID
import scala.collection.mutable

object ProtonCapability {
  class Provider extends ICapabilitySerializable[NBTTagCompound] {
    private val instance = new Impl

    private val storage = new Storage

    override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == protonCapability

    override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
      if (protonCapability == null) println("Null Cap!")
      if (capability == protonCapability) protonCapability.cast(instance) else null.asInstanceOf[T]
    }

    override def serializeNBT(): NBTTagCompound = {
      val tag = new NBTTagCompound
      tag.setTag("proton", storage.writeNBT(protonCapability, instance, null))
      tag
    }

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {
      val tag = nbt.getCompoundTag("proton")
      storage.readNBT(protonCapability, instance, null, tag)
    }
  }

  class Storage extends IStorage[IProtonCapability] {
    override def writeNBT(capability: Capability[IProtonCapability], instance: IProtonCapability, side: EnumFacing): NBTBase = {
      val tag = new NBTTagCompound
      val g = instance.getGroups
      if (g.nonEmpty) {
        val groupsTag = new NBTTagList
        for (group <- g) groupsTag.appendTag(new NBTTagString(group.getName))
        tag.setTag("protonGroups", groupsTag)
      }
      val p = instance.getPerms
      if (p.nonEmpty) {
        val nodesTag = new NBTTagList
        for (node <- p) nodesTag.appendTag(new NBTTagString(node.getName))
        tag.setTag("protonNodes", nodesTag)
      }
      tag.setBoolean("protonInitialized", instance.wasInitialized)
      tag
    }

    override def readNBT(capability: Capability[IProtonCapability], instance: IProtonCapability, side: EnumFacing, nbt: NBTBase): Unit = {
      if (!nbt.isInstanceOf[NBTTagCompound]) return
      val tag = nbt.asInstanceOf[NBTTagCompound]
      val groups = instance.getGroupsMutable
      val permNodes = instance.getPermsMutable
      groups.clear
      permNodes.clear()
      instance.setInitialized(tag.getBoolean("protonInitialized"))
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
  }

  class Impl extends IProtonCapability {
    private var changingUUID: UUID = UUID.randomUUID()
    private var initialized = false
    private val groups: mutable.HashSet[IGroup] = new mutable.HashSet[IGroup]()
    private val permNodes: mutable.HashSet[IPermNode] = new mutable.HashSet[IPermNode]()

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

    override def getPermsMutable: mutable.HashSet[IPermNode] = permNodes

    override def getGroupsMutable: mutable.HashSet[IGroup] = groups

    def addPerm(perm: IPermNode): Boolean = permNodes.add(perm)

    def removePerm(perm: IPermNode): Boolean = permNodes.remove(perm)

    def addToGroup(group: IGroup): Boolean = groups.add(group)

    def removeFromGroup(group: IGroup): Boolean = groups.remove(group)

    def dissociate(group: IGroup): Boolean = {
      permNodes.++=(group.getPerms)
      removeFromGroup(group)
    }
  }
}
