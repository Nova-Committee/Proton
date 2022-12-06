package committee.nova.proton.api.perm

import committee.nova.proton.core.perm.PermNode
import committee.nova.proton.core.storage.ProtonSavedData
import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable

trait IGroup {
  def getName: String

  def getPerms: mutable.HashSet[IPermNode]

  def getChildren: mutable.HashSet[String]

  def deserialize(tag: NBTTagCompound): IGroup = {
    val permList = tag.getTagList("perms", 9)
    for (i <- 0 until permList.tagCount()) {
      val perm = PermNode(permList.getStringTagAt(i))
      ProtonSavedData.get.getPermNodes.add(perm)
      getPerms.add(perm)
    }
    val childrenList = tag.getTagList("children", 9)
    for (i <- 0 until childrenList.tagCount()) getChildren.add(childrenList.getStringTagAt(i))
    this
  }

  def serialize: NBTTagCompound = {

  }
}
