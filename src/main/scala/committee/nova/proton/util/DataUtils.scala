package committee.nova.proton.util

import net.minecraft.world.WorldServer
import net.minecraftforge.fml.server.FMLServerHandler

object DataUtils {
  def getOverworld: WorldServer = FMLServerHandler.instance().getServer.getWorld(0)

  def getOverworldName: String = getOverworld.getWorldInfo.getWorldName
}
