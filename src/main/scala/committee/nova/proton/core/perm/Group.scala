package committee.nova.proton.core.perm

import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.server.storage.ProtonSavedData

import scala.collection.mutable

case class Group(name: String) extends IGroup {
  private val perms = new mutable.HashSet[IPermNode]()
  private val children = new mutable.HashSet[String]()

  override def getName: String = name

  override def getPerms: Array[IPermNode] = perms.toArray

  override def getChildren: mutable.HashSet[String] = children

  override def addPerm(perm: IPermNode): Boolean = perms.add(perm)

  override def removePerm(perm: IPermNode): Boolean = {
    children.foreach(c => ProtonSavedData.get.getGroup(c).foreach(g => g.removePerm(perm)))
    perms.remove(perm)
  }

  override def inherit(that: IGroup): Unit = perms.++=(that.getPerms)
}
