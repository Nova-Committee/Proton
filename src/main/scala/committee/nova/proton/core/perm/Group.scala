package committee.nova.proton.core.perm

import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.server.storage.ProtonSavedData

import scala.collection.mutable

case class Group(name: String) extends IGroup {
  private val perms = new mutable.HashSet[IPermNode]()
  private val children = new mutable.HashSet[String]()
  private var immutable = false

  override def getName: String = name

  override def getPerms: Array[IPermNode] = perms.toArray

  override def isImmutable: Boolean = immutable

  override def setImmutable(immutable: Boolean): Unit = this.immutable = immutable

  override def getChildren: Array[String] = children.toArray

  override def addChild(child: String): Boolean = children.add(child)

  override def removeChild(child: String): Boolean = children.remove(child)

  override def addPerm(perm: IPermNode): Boolean = {
    children.foreach(c => ProtonSavedData.get.getGroup(c).foreach(g => g.addPerm(perm)))
    perms.add(perm)
  }

  override def removePerm(perm: IPermNode): Boolean = {
    children.foreach(c => ProtonSavedData.get.getGroup(c).foreach(g => g.removePerm(perm)))
    perms.remove(perm)
  }

  override def inherit(that: IGroup): Unit = {
    perms.++=(that.getPerms)
    that.addChild(this.getName)
  }

  override def dissociate(that: IGroup): Boolean = that.removeChild(this.getName)
}
