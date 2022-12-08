package committee.nova.proton.core.event.handler

import committee.nova.proton.core.perm.PermNode
import cpw.mods.fml.common.FMLCommonHandler

object FMLEventHandler {
  def init(): Unit = FMLCommonHandler.instance().bus().register(new FMLEventHandler)

  val testNode1: PermNode = PermNode("proton.test.1")
  val testNode2: PermNode = PermNode("proton.test.2")
}

class FMLEventHandler {
}
