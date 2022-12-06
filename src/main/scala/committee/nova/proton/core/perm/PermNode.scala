package committee.nova.proton.core.perm

import committee.nova.proton.api.perm.IPermNode

case class PermNode(name: String) extends IPermNode {
  override def getName: String = name
}
