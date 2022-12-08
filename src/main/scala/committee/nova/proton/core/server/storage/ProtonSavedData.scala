package committee.nova.proton.core.server.storage

import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.config.ServerConfig
import committee.nova.proton.core.command.impl.CommandProton
import committee.nova.proton.core.event.impl.{ProtonGroupInitializationEvent, ProtonPermNodeInitializationEvent}
import committee.nova.proton.core.perm.{Group, PermNode}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData

import java.util.UUID
import scala.collection.mutable

object ProtonSavedData {
  private var instance: ProtonSavedData = _

  def get: ProtonSavedData = {
    val world = MinecraftServer.getServer.worldServers(0)
    if (instance != null) return instance
    val data = world.mapStorage.loadData(classOf[ProtonSavedData], "ProtonData")
    if (data != null) {
      instance = data.asInstanceOf[ProtonSavedData]
      return instance
    }
    instance = new ProtonSavedData
    world.mapStorage.setData("ProtonData", instance)
    instance
  }
}

class ProtonSavedData extends WorldSavedData("Proton") {
  private val groups = new mutable.HashSet[IGroup]()
  private val permNodes = new mutable.HashSet[IPermNode]()
  private var changingUUID = UUID.randomUUID()

  groups.clear()
  permNodes.clear()
  permNodes.++=(ProtonPermNodeInitializationEvent.getPermNodes)
  if (!ServerConfig.wasInitialized) {
    groups.++=(ProtonGroupInitializationEvent.getAllGroups)
    ServerConfig.setInitialized(true)
    ServerConfig.sync()
  }

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

  def addPermNode(node: IPermNode): Boolean = permNodes.add(node)

  def getChangingUUID: UUID = changingUUID

  override def readFromNBT(tag: NBTTagCompound): Unit = {
    if (tag.hasKey("protonGroups")) {
      val groupsTag = tag.getTagList("protonGroups", 10)
      for (i <- 0 until groupsTag.tagCount()) {
        val group = groupsTag.getCompoundTagAt(i)
        groups.add(Group(group.getString("name")).deserialize(group))
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
