package committee.nova.proton.core.event.handler

import committee.nova.proton.core.event.impl.ProtonPlayerInitializationEvent
import committee.nova.proton.implicits.PlayerImplicit
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent

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
