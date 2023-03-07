package committee.nova.proton.core.event.impl

import committee.nova.proton.api.perm.IPermNode
import committee.nova.proton.core.event.impl.ProtonPermNodeInitializationEvent.{getPermNodes, permNodes}
import committee.nova.proton.core.perm.PermNode
import net.minecraftforge.fml.common.eventhandler.Event

import scala.collection.mutable

object ProtonPermNodeInitializationEvent {
  private val permNodes = new mutable.HashSet[IPermNode]()

  def getPermNodes: Iterator[IPermNode] = permNodes.toIterator
}

class ProtonPermNodeInitializationEvent extends Event {
  def addNode(nodes: IPermNode*): Unit = for (node <- nodes) permNodes.add(node)

  def addNodeFromString(nodes: String*): Unit = for (node <- nodes) permNodes.add(PermNode(node))

  def getNodes: Iterator[IPermNode] = getPermNodes
}
