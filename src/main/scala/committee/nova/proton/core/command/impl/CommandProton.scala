package committee.nova.proton.core.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.config.ServerConfig
import committee.nova.proton.core.l10n.ChatComponentServerTranslation
import committee.nova.proton.core.server.storage.ProtonSavedData
import committee.nova.proton.implicits.PlayerImplicit
import committee.nova.proton.util.{L10nUtils, PlayerUtils, StringUtils}
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{Style, TextComponentString, TextFormatting}

import java.util
import scala.collection.JavaConverters._

object CommandProton {
  val instance = new CommandProton

  final val keywords = "group" :: "player" :: "create" :: "remove" :: "listgroups" :: "listperms" :: "inherit" :: "padd" :: "pdel" :: "gadd" :: "gdel" :: Nil
}

class CommandProton extends CommandBase {
  override def getName: String = "proton"

  override def getUsage(sender: ICommandSender): String = "msg.proton.cmd.help"

  override def execute(server: MinecraftServer, sender: ICommandSender, args: Array[String]): Unit = {
    args.length match {
      case 1 =>
        args(0) match {
          case "listgroups" => sender.sendMessage(new TextComponentString(StringUtils.convertIteratorToString(ProtonSavedData.get.getGroups.toIterator, (g: IGroup, _) => g.getName,
            L10nUtils.getFromCurrentLang("msg.proton.group.none"))))
          case "listperms" => sender.sendMessage(new TextComponentString(StringUtils.convertIteratorToString(ProtonSavedData.get.getPermNodes.toIterator, (p: IPermNode, _) => p.getName,
            L10nUtils.getFromCurrentLang("msg.proton.perm.none"))))
          case "help" =>
            Array(
              "reload", "listgroups", "listperms", "group create [Group]", "group remove [Group]",
              "group [Group] listperms", "group [GroupA] inherit [GroupB]", "group [GroupA] dissociate [GroupB]", "group [GroupA] create [GroupB]",
              "group [Group] padd [Perm]", "group [Group] pdel [Perm]",
              "player [PlayerName] listgroups", "player [PlayerName] listperms", "player [PlayerName] padd [Perm]", "player [PlayerName] pdel [Perm]",
              "player [PlayerName] gadd [Group]", "player [PlayerName] gdel [Group]", "player [PlayerName] dissociate [Group]"
            ).map(t => new TextComponentString(s"/proton $t")).foreach(t => sender.sendMessage(t))
          case "reload" =>
            sender.sendMessage(new ChatComponentServerTranslation("msg.proton.reload.inProgress")
              .setStyle(new Style().setColor(TextFormatting.YELLOW)))
            try {
              ServerConfig.sync()
            } catch {
              case e: Exception =>
                e.printStackTrace()
                sender.sendMessage(new ChatComponentServerTranslation("msg.proton.reload.error")
                  .setStyle(new Style().setColor(TextFormatting.DARK_RED)))
                return
            }
            sender.sendMessage(new ChatComponentServerTranslation("msg.proton.reload.success")
              .setStyle(new Style().setColor(TextFormatting.GREEN)))
          case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
            .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        }
      case 3 =>
        args(0) match {
          case "group" =>
            args(1) match {
              case "create" =>
                val success = ProtonSavedData.get.createGroup(args(2))
                sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.group.create.${if (success) "success" else "failure"}", args(2))
                  .setStyle(new Style().setColor(if (success) TextFormatting.GREEN else TextFormatting.RED)))
              case "remove" =>
                if (ProtonSavedData.get.getGroups.exists(g => g.isImmutable && args(2).equals(g.getName))) {
                  sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.immutable", args(2))
                    .setStyle(new Style().setColor(TextFormatting.YELLOW)))
                  return
                }
                val success = ProtonSavedData.get.removeGroup(args(2))
                sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.group.remove.${if (success) "success" else "failure"}", args(2))
                  .setStyle(new Style().setColor(if (success) TextFormatting.GREEN else TextFormatting.RED)))
              case y if ProtonSavedData.get.getGroup(y).isDefined =>
                args(2) match {
                  case "listperms" => ProtonSavedData.get.getGroup(y).foreach(g => {
                    sender.sendMessage(new TextComponentString(g.getName + ":"))
                    sender.sendMessage(new TextComponentString(StringUtils.convertIteratorToString(g.getPerms.toIterator, (p: IPermNode, _) => p.getName,
                      L10nUtils.getFromCurrentLang("msg.proton.perm.none"))))
                  })
                  case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
                    .setStyle(new Style().setColor(TextFormatting.YELLOW)))
                }
              case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
                .setStyle(new Style().setColor(TextFormatting.YELLOW)))
            }
          case "player" =>
            PlayerUtils.getPlayer(server, sender, args(1)).foreach(player => {
              args(2) match {
                case "listgroups" =>
                  sender.sendMessage(new TextComponentString(player.getName + ":"))
                  sender.sendMessage(new TextComponentString(StringUtils.convertIteratorToString(player.getGroups.toIterator, (g: IGroup, _) => g.getName,
                    L10nUtils.getFromCurrentLang("msg.proton.group.none"))))
                  return
                case "listperms" =>
                  var raw = player.getPerms.toSet
                  for (group <- player.getGroups) raw = raw.++(group.getPerms)
                  sender.sendMessage(new TextComponentString(player.getName + ":"))
                  sender.sendMessage(new TextComponentString(StringUtils.convertIteratorToString(raw.toIterator, (p: IPermNode, _) => p.getName,
                    L10nUtils.getFromCurrentLang("msg.proton.perm.none"))))
                  return
                case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
                  .setStyle(new Style().setColor(TextFormatting.YELLOW)))
                  return
              }
            })
            sender.sendMessage(new ChatComponentServerTranslation("msg.proton.player.notFound", args(1))
              .setStyle(new Style().setColor(TextFormatting.RED)))
          case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
            .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        }
      case 4 =>
        args(0) match {
          case "group" =>
            ProtonSavedData.get.getGroup(args(1)).foreach(group => {
              args(2) match {
                case "inherit" =>
                  if (args(1).equals(args(3))) {
                    sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.inherit.self")
                      .setStyle(new Style().setColor(TextFormatting.RED)))
                    return
                  }
                  ProtonSavedData.get.getGroup(args(3)).foreach(target => {
                    group.inherit(target)
                    sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.inherited", group.getName, target.getName)
                      .setStyle(new Style().setColor(TextFormatting.GREEN)))
                    return
                  })
                  sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.notFound", args(3))
                    .setStyle(new Style().setColor(TextFormatting.RED)))
                  return
                case "dissociate" =>
                  if (args(1).equals(args(3))) {
                    sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.dissociate.self")
                      .setStyle(new Style().setColor(TextFormatting.RED)))
                    return
                  }
                  ProtonSavedData.get.getGroup(args(3)).foreach(target => {
                    val success = group.dissociate(target)
                    sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.group.dissociate.${if (success) "success" else "failure"}", target.getName, group.getName)
                      .setStyle(new Style().setColor(if (success) TextFormatting.GREEN else TextFormatting.RED)))
                    return
                  })
                  sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.notFound", args(3))
                    .setStyle(new Style().setColor(TextFormatting.RED)))
                  return
                case "create" =>
                  val manager = server.getCommandManager
                  if (manager.executeCommand(sender, s"/proton group create ${args(3)}") > 0)
                    manager.executeCommand(sender, s"/proton group ${args(3)} inherit ${args(1)}")
                  return
                case "padd" =>
                  if (group.isImmutable) {
                    sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.immutable", group.getName)
                      .setStyle(new Style().setColor(TextFormatting.YELLOW)))
                    return
                  }
                  val added = group.addPerm(args(3))
                  sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.group.addPerm.${if (added) "success" else "failure"}", args(3), group.getName)
                    .setStyle(new Style().setColor(if (added) TextFormatting.GREEN else TextFormatting.RED)))
                  return
                case "pdel" =>
                  if (group.isImmutable) {
                    sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.immutable", group.getName)
                      .setStyle(new Style().setColor(TextFormatting.YELLOW)))
                    return
                  }
                  val removed = group.removePerm(args(3))
                  sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.group.delPerm.${if (removed) "success" else "failure"}", args(3), group.getName)
                    .setStyle(new Style().setColor(if (removed) TextFormatting.GREEN else TextFormatting.RED)))
                  return
                case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
                  .setStyle(new Style().setColor(TextFormatting.YELLOW)))
                  return
              }
            })
            sender.sendMessage(new ChatComponentServerTranslation("msg.proton.group.notFound", args(1))
              .setStyle(new Style().setColor(TextFormatting.RED)))
          case "player" =>
            PlayerUtils.getPlayer(server, sender, args(1)).foreach(player => {
              args(2) match {
                case "padd" =>
                  val added = player.addPerm(args(3))
                  sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.player.addPerm.${if (added) "success" else "failure"}", args(3), player.getName)
                    .setStyle(new Style().setColor(if (added) TextFormatting.GREEN else TextFormatting.RED)))
                  return
                case "pdel" =>
                  val removed = player.removePerm(args(3))
                  sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.player.delPerm.${if (removed) "success" else "failure"}", args(3), player.getName)
                    .setStyle(new Style().setColor(if (removed) TextFormatting.GREEN else TextFormatting.RED)))
                  return
                case "gadd" =>
                  val added = player.addToGroup(args(3))
                  sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.player.addToGroup.${if (added) "success" else "failure"}", args(3), player.getName)
                    .setStyle(new Style().setColor(if (added) TextFormatting.GREEN else TextFormatting.RED)))
                  return
                case "gdel" =>
                  val removed = player.removeFromGroup(args(3))
                  sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.player.removeFromGroup.${if (removed) "success" else "failure"}", args(3), player.getName)
                    .setStyle(new Style().setColor(if (removed) TextFormatting.GREEN else TextFormatting.RED)))
                  return
                case "dissociate" =>
                  val dissociated = player.dissociate(args(3))
                  sender.sendMessage(new ChatComponentServerTranslation(s"msg.proton.player.dissociate.${if (dissociated) "success" else "failure"}", args(3), player.getName)
                    .setStyle(new Style().setColor(if (dissociated) TextFormatting.GREEN else TextFormatting.RED)))
                  return
                case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
                  .setStyle(new Style().setColor(TextFormatting.YELLOW)))
                  return
              }
            })
            sender.sendMessage(new ChatComponentServerTranslation("msg.proton.player.notFound", args(1))
              .setStyle(new Style().setColor(TextFormatting.RED)))
          case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
            .setStyle(new Style().setColor(TextFormatting.YELLOW)))
        }
      case _ => sender.sendMessage(new ChatComponentServerTranslation(getUsage(sender))
        .setStyle(new Style().setColor(TextFormatting.YELLOW)))
    }
  }

  override def getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array[String], blockPos: BlockPos): util.List[String] = {
    args.length match {
      case 1 => CommandBase.getListOfStringsMatchingLastWord(args, Array("group", "player", "listgroups", "listperms", "help", "reload"): _*)
      case 2 => CommandBase.getListOfStringsMatchingLastWord(args, (args(0) match {
        case "group" => "create" :: ProtonSavedData.get.getGroups.map(g => g.getName).toList
        case "player" => server.getPlayerList.getPlayers.asScala.map(p => p.getName).toList
        case _ => Nil
      }): _*)
      case 3 => CommandBase.getListOfStringsMatchingLastWord(args, (args(0) match {
        case "group" =>
          args(1) match {
            case g if ProtonSavedData.get.getGroup(g).isDefined => Array("dissociate", "create", "listperms").toBuffer
              .++=(if (ProtonSavedData.get.getGroup(g).get.isImmutable) Array("") else Array("inherit", "padd", "pdel")).toArray
            case "remove" => ProtonSavedData.get.getGroups.filter(g => !g.isImmutable).map(g => g.getName)
            case _ => Array("")
          }
        case "player" =>
          args(1) match {
            case p if server.getPlayerList.getPlayers.asScala.map(p => p.getName).contains(p) => Array("padd", "pdel", "gadd", "gdel", "dissociate", "listperms", "listgroups")
            case _ => Array("")
          }
        case _ => Array("")
      }): _*)
      case 4 => CommandBase.getListOfStringsMatchingLastWord(args, (args(0) match {
        case "group" =>
          ProtonSavedData.get.getGroup(args(1)) match {
            case Some(g) => args(2) match {
              case "inherit" => ProtonSavedData.get.getGroups.filter(t => !t.equals(g) && !t.getChildren.contains(g.getName)).map(t => t.getName)
              case "padd" => ProtonSavedData.get.getPermNodes.toBuffer.--=(g.getPerms).map(p => p.getName).toArray
              case "pdel" => g.getPerms.map(p => p.getName)
              case "dissociate" => ProtonSavedData.get.getGroups.filter(t => t.getChildren.contains(g.getName)).map(t => t.getName)
              case _ => Array("")
            }
            case None => Array("")
          }
        case "player" =>
          PlayerUtils.getPlayer(server, sender, args(1)) match {
            case Some(p) => args(2) match {
              case "gadd" => ProtonSavedData.get.getGroups.toBuffer.--=(p.getGroups).map(g => g.getName).toArray
              case "gdel" => p.getGroups.map(g => g.getName)
              case "padd" => ProtonSavedData.get.getPermNodes.toBuffer.--=(p.getPerms).map(p => p.getName).toArray
              case "pdel" => p.getPerms.map(p => p.getName)
              case "dissociate" => p.getGroups.map(g => g.getName)
              case _ => Array("")
            }
            case None => Array("")
          }
        case _ => Array("")
      }): _*)
      case _ => ImmutableList.of()
    }
  }
}
