package committee.nova.proton.api.perm

import committee.nova.proton.util.PermUtils

trait IPermNode {
  def getName: String

  def isChildOf(that: String): Boolean = getName.contains(that) && !getName.equals(that)

  def isChildOf(that: IPermNode): Boolean = getName.contains(that.getName) && !getName.equals(that.getName)

  def isParentOf(that: String): Boolean = that.contains(getName) && !getName.equals(that)

  def isParentOf(that: IPermNode): Boolean = that.isChildOf(this) && !getName.equals(that.getName)

  def covers(that: IPermNode): Boolean = PermUtils.isCovered(this, that)

  override def hashCode(): Int = getName.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case s: String => s.equals(getName)
    case n: IPermNode => getName.equals(n.getName)
    case _ => false
  }
}
