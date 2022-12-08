package committee.nova.proton.api.perm

import committee.nova.proton.core.perm.PermNode
import committee.nova.proton.core.server.storage.ProtonSavedData
import committee.nova.proton.util.PermUtils
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}

import scala.collection.mutable

trait IGroup {
  def getName: String

  def getPerms: Array[IPermNode]

  def addPerm(perm: IPermNode): Boolean

  def addPerm(perm: String): Boolean = {
    val nodes = ProtonSavedData.get.getPermNodes
    nodes.find(p => perm.equals(p.getName)) match {
      case Some(p) => addPerm(p)
      case None => nodes.find(p => PermUtils.isCovered(perm, p.getName)) match {
        case Some(p) => addPerm(p)
        case None => false
      }
    }
  }

  def removePerm(perm: IPermNode): Boolean

  def removePerm(perm: String): Boolean = {
    if (perm.endsWith("*")) {
      var removed = false
      val toRemove = getPerms.filter(p => p.getName.startsWith(perm.replaceAllLiterally("*", "")))
      for (p <- toRemove) if (removePerm(p)) removed = true
      return removed
    }
    getPerms.find(p => perm.equals(p.getName)) match {
      case Some(p) => removePerm(p)
      case None => false
    }
  }

  def getChildren: mutable.HashSet[String]

  def hasPerm(node: IPermNode): Boolean = getPerms.contains(node) || getPerms.exists(p => PermUtils.isCovered(p, node))

  def hasPerm(node: String): Boolean = getPerms.exists(p => node.equals(p.getName)) || getPerms.exists(p => PermUtils.isCovered(p.getName, node))

  def inherit(that: IGroup): Unit

  def intersect(that: IGroup): Array[IPermNode] = {
    val common = new mutable.HashSet[IPermNode]()
    getPerms.foreach(p => if (that.getPerms.contains(p)) common.add(p))
    common.toArray
  }

  def deserialize(tag: NBTTagCompound): IGroup = {
    val permList = tag.getTagList("perms", 8)
    for (i <- 0 until permList.tagCount()) {
      val perm = PermNode(permList.getStringTagAt(i))
      if (!perm.getName.endsWith("*")) ProtonSavedData.get.addPermNode(perm)
      addPerm(perm)
    }
    val childrenList = tag.getTagList("children", 8)
    for (i <- 0 until childrenList.tagCount()) getChildren.add(childrenList.getStringTagAt(i))
    this
  }

  def serialize: NBTTagCompound = {
    val tag = new NBTTagCompound
    val permList = new NBTTagList
    for (perm <- getPerms) permList.appendTag(new NBTTagString(perm.getName))
    tag.setTag("perms", permList)
    val childrenList = new NBTTagList
    for (child <- getChildren) childrenList.appendTag(new NBTTagString(child))
    tag.setTag("children", childrenList)
    tag
  }

  override def hashCode(): Int = getName.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case g: IGroup => getName.equals(g.getName)
    case _ => false
  }
}
