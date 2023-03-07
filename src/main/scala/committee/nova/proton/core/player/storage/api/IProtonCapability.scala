package committee.nova.proton.core.player.storage.api

import committee.nova.proton.api.perm.{IGroup, IPermNode}

import scala.collection.mutable

trait IProtonCapability {
  def wasInitialized: Boolean

  def setInitialized(initialized: Boolean): Unit

  def hasPerm(node: IPermNode): Boolean

  def hasPerm(node: String): Boolean

  def checkGroups(): Unit

  def getPerms: Array[IPermNode]

  def getGroups: Array[IGroup]

  def getPermsMutable: mutable.HashSet[IPermNode]

  def getGroupsMutable: mutable.HashSet[IGroup]

  def addPerm(perm: IPermNode): Boolean

  def removePerm(perm: IPermNode): Boolean

  def addToGroup(group: IGroup): Boolean

  def removeFromGroup(group: IGroup): Boolean

  def dissociate(group: IGroup): Boolean
}
