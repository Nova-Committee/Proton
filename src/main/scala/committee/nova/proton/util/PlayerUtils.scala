package committee.nova.proton.util

import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayerMP

import scala.util.Try

object PlayerUtils {
  def getPlayer(sender: ICommandSender, name: String): Option[EntityPlayerMP] = Try(CommandBase.getPlayer(sender, name)).toOption
}
