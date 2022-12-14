package committee.nova.proton.core.server.storage

import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.command.impl.CommandProton
import committee.nova.proton.core.event.impl.{ProtonImmutableGroupInitializationEvent, ProtonPermNodeInitializationEvent}
import committee.nova.proton.core.perm.{Group, PermNode}
import committee.nova.proton.util.DataUtils
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.world.WorldSavedData

import java.util.UUID
import scala.collection.mutable

object ProtonSavedData {
  def get: ProtonSavedData = {
    val world = DataUtils.getOverworld
    var data = world.mapStorage.loadData(classOf[ProtonSavedData], "ProtonData")
    if (data != null) return data.asInstanceOf[ProtonSavedData]
    data = new ProtonSavedData("ProtonData")
    world.mapStorage.setData("ProtonData", data)
    data.asInstanceOf[ProtonSavedData]
  }

  private val permNodeCache: mutable.HashSet[IPermNode] = new mutable.HashSet[IPermNode]()

  def appendIntoCache(perm: IPermNode): Unit = permNodeCache.add(perm)

  def processCache(): Unit = {
    permNodeCache.foreach(p => get.addPermNode(p))
    permNodeCache.clear()
  }
}

class ProtonSavedData(name: String) extends WorldSavedData(name) {
  private val groups = new mutable.HashSet[IGroup]()
  private val permNodes = new mutable.HashSet[IPermNode]()
  private var changingUUID = UUID.randomUUID()

  permNodes.++=(ProtonPermNodeInitializationEvent.getPermNodes)
  groups.++=(ProtonImmutableGroupInitializationEvent.getImmutableGroups)

  def getGroups: Array[IGroup] = groups.toArray

  def getPermNodes: Array[IPermNode] = permNodes.toArray

  def createGroup(name: String): Boolean = {
    if (groups.exists(g => name.equals(g.getName)) || CommandProton.keywords.contains(name)) return false
    val added = groups.add(Group(name))
    if (added) markDirty()
    added
  }

  def removeGroup(group: IGroup): Boolean = {
    val removed = groups.remove(group)
    if (removed) {
      changingUUID = UUID.randomUUID()
      markDirty()
    }
    removed
  }

  def removeGroup(group: String): Boolean = {
    groups.find(g => group.equals(g.getName)) match {
      case Some(g) =>
        val removed = groups.remove(g)
        if (removed) {
          changingUUID = UUID.randomUUID()
          markDirty()
        }
        removed
      case None => false
    }
  }

  def getGroup(name: String): Option[IGroup] = groups.find(g => name.equals(g.getName))

  def addPermNode(node: IPermNode): Boolean = {
    val added = permNodes.add(node)
    if (added) markDirty()
    added
  }

  def getChangingUUID: UUID = changingUUID

  override def readFromNBT(tag: NBTTagCompound): Unit = {
    if (tag.hasKey("protonGroups")) {
      val groupsTag = tag.getTagList("protonGroups", 10)
      for (i <- 0 until groupsTag.tagCount()) {
        val groupInfo = groupsTag.getCompoundTagAt(i)
        val groupName = groupInfo.getString("name")
        val group = Group(groupName).deserialize(groupInfo)
        groups.add(group)
      }
    }
    if (tag.hasKey("protonNodes")) {
      val nodesTag = tag.getTagList("protonNodes", 8)
      for (i <- 0 until nodesTag.tagCount()) permNodes.add(PermNode(nodesTag.getStringTagAt(i)))
    }
    markDirty()
  }

  override def writeToNBT(tag: NBTTagCompound): Unit = {
    if (groups.nonEmpty) {
      val groupsTag = new NBTTagList
      for (group <- groups) {
        val serialized = group.serialize
        serialized.setString("name", group.getName)
        groupsTag.appendTag(serialized)
      }
      tag.setTag("protonGroups", groupsTag)
    }
    if (permNodes.nonEmpty) {
      val nodesTag = new NBTTagList
      for (node <- permNodes) nodesTag.appendTag(new NBTTagString(node.getName))
      tag.setTag("protonNodes", nodesTag)
    }
  }
}
