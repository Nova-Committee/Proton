package committee.nova.proton.util

import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer

import scala.util.Try

object PlayerUtils {
  def getPlayer(server: MinecraftServer, sender: ICommandSender, name: String): Option[EntityPlayerMP] = Try(CommandBase.getPlayer(server, sender, name)).toOption
}
