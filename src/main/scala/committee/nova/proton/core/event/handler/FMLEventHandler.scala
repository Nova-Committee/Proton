package committee.nova.proton.core.event.handler

import committee.nova.proton.core.event.impl.ProtonPlayerInitializationEvent
import committee.nova.proton.implicits.PlayerImplicit
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent
import net.minecraft.entity.player.EntityPlayerMP

object FMLEventHandler {
  def init(): Unit = FMLCommonHandler.instance().bus().register(new FMLEventHandler)
}

class FMLEventHandler {
  @SubscribeEvent
  def onLogin(e: PlayerLoggedInEvent): Unit = {
    val player = e.player
    player match {
      case mp: EntityPlayerMP =>
        if (mp.wasInitialized) return
        ProtonPlayerInitializationEvent.getAllFuncs.foreach(f => f.apply(mp))
        mp.setInitialized(true)
    }
  }
}
