package committee.nova.proton.util

import committee.nova.proton.api.perm.IPermNode

object PermUtils {
  def isCovered(perm: String, target: String): Boolean = perm.endsWith("*") && target.startsWith(perm.replaceAllLiterally("*", ""))

  def isCovered(perm: IPermNode, target: IPermNode): Boolean = isCovered(perm.getName, target.getName)
}
