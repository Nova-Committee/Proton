package committee.nova.proton.core.perm

import committee.nova.proton.api.perm.{IGroup, IPermNode}

import scala.collection.mutable

case class Group(name: String) extends IGroup {
  private val perms = new mutable.HashSet[IPermNode]()
  private val children = new mutable.HashSet[String]()

  override def getName: String = name

  override def getPerms: mutable.HashSet[IPermNode] = perms

  override def getChildren: mutable.HashSet[String] = children
}
