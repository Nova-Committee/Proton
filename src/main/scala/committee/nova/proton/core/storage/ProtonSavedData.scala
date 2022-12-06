package committee.nova.proton.core.storage

import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.perm.Group
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData

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

  def getGroups: mutable.HashSet[IGroup] = groups

  def getPermNodes: mutable.HashSet[IPermNode] = permNodes

  override def readFromNBT(tag: NBTTagCompound): Unit = {
    groups.clear()
    permNodes.clear()
    if (!tag.hasKey("protonGroups")) return
    val groupsTag = tag.getTagList("protonGroups", 10)
    for (i <- 0 until groupsTag.tagCount()) {
      val group = groupsTag.getCompoundTagAt(i)
      groups.add(Group(group.getString("name")).deserialize(group))
    }
    markDirty()
  }

  override def writeToNBT(tag: NBTTagCompound): Unit = {
    val groupsTag = new NBTTagList
    for (group <- groups) groupsTag.appendTag(group.serialize)
    tag.setTag("protonGroups", groupsTag)
  }
}
