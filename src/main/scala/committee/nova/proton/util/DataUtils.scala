package committee.nova.proton.util

import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldServer

object DataUtils {
  def getOverworld: WorldServer = MinecraftServer.getServer.worldServers(0)

  def getOverworldName: String = getOverworld.getWorldInfo.getWorldName
}
