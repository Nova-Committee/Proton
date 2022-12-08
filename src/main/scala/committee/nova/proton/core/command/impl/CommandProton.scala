package committee.nova.proton.core.command.impl

import com.google.common.collect.ImmutableList
import committee.nova.proton.api.perm.{IGroup, IPermNode}
import committee.nova.proton.core.l10n.ChatComponentServerTranslation
import committee.nova.proton.core.server.storage.ProtonSavedData
import committee.nova.proton.implicits.PlayerImplicit
import committee.nova.proton.util.{PlayerUtils, StringUtils}
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChatComponentText

import java.util

object CommandProton {
  val instance = new CommandProton

  final val keywords = "group" :: "player" :: "create" :: "remove" :: "listgroups" :: "listperms" :: "inherit" :: "padd" :: "pdel" :: "gadd" :: "gdel" :: Nil
}

class CommandProton extends CommandBase {
  override def getCommandName: String = "proton"

  override def getCommandUsage(sender: ICommandSender): String = {
    // TODO:
    ""
  }

  override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
    args.length match {
      case 1 =>
        args(0) match {
          case "listgroups" => sender.addChatMessage(new ChatComponentText(StringUtils.convertIteratorToString(ProtonSavedData.get.getGroups.toIterator, (g: IGroup, _) => g.getName)))
          case "listperms" => sender.addChatMessage(new ChatComponentText(StringUtils.convertIteratorToString(ProtonSavedData.get.getPermNodes.toIterator, (p: IPermNode, _) => p.getName)))
        }
      case 3 =>
        args(0) match {
          case "group" =>
            args(1) match {
              case "create" =>
                sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.group.create.${if (ProtonSavedData.get.createGroup(args(2))) "success" else "failure"}"))
              case "remove" =>
                sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.group.remove.${if (ProtonSavedData.get.removeGroup(args(2))) "success" else "failure"}"))
              case y if ProtonSavedData.get.getGroup(y).isDefined => {
                args(2) match {
                  case "listperms" => ProtonSavedData.get.getGroup(y).foreach(g => {
                    sender.addChatMessage(new ChatComponentText(g.getName + ":"))
                    sender.addChatMessage(new ChatComponentText(StringUtils.convertIteratorToString(g.getPerms.toIterator, (p: IPermNode, _) => p.getName)))
                  })
                  case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
                }
              }
              case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
            }
          case "player" => {
            PlayerUtils.getPlayer(sender, args(1)).foreach(player => {
              args(2) match {
                case "listgroups" => {
                  sender.addChatMessage(new ChatComponentText(player.getCommandSenderName + ":"))
                  sender.addChatMessage(new ChatComponentText(StringUtils.convertIteratorToString(player.getGroups.toIterator, (g: IGroup, _) => g.getName)))
                  return
                }
                case "listperms" => {
                  var raw = player.getPerms.toList
                  for (group <- player.getGroups) raw = (raw :: group.getPerms.toList).asInstanceOf[List[IPermNode]]
                  sender.addChatMessage(new ChatComponentText(player.getCommandSenderName + ":"))
                  sender.addChatMessage(new ChatComponentText(StringUtils.convertIteratorToString(raw.toIterator, (p: IPermNode, _) => p.getName)))
                  return
                }
                case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
                  return
              }
            })
            sender.addChatMessage(new ChatComponentServerTranslation("msg.proton.player.notFound", args(1)))
          }
          case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        }
      case 4 => {
        args(0) match {
          case "group" => {
            ProtonSavedData.get.getGroup(args(1)).foreach(group => {
              args(2) match {
                case "inherit" => {
                  ProtonSavedData.get.getGroup(args(3)).foreach(target => {
                    group.inherit(target)
                    sender.addChatMessage(new ChatComponentServerTranslation("msg.proton.group.inherited", group.getName, target.getName))
                    return
                  })
                  sender.addChatMessage(new ChatComponentServerTranslation("msg.proton.group.notFound", args(3)))
                  return
                }
                case "create" => {
                  val manager = MinecraftServer.getServer.getCommandManager
                  val i = manager.executeCommand(sender, s"/proton group create ${args(3)}")
                  if (i > 0) manager.executeCommand(sender, s"/proton group ${args(3)} inherit ${args(1)}")
                  return
                }
                case "padd" => {
                  val added = group.addPerm(args(3))
                  sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.group.addPerm.${if (added) "success" else "failure"}"))
                  return
                }
                case "pdel" => {
                  val removed = group.removePerm(args(3))
                  sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.group.delPerm.${if (removed) "success" else "failure"}"))
                  return
                }
                case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
                  return
              }
            })
            sender.addChatMessage(new ChatComponentServerTranslation("msg.proton.group.notFound", args(1)))
          }
          case "player" => {
            PlayerUtils.getPlayer(sender, args(1)).foreach(player => {
              args(2) match {
                case "padd" => {
                  val added = player.addPerm(args(3))
                  sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.player.addPerm.${if (added) "success" else "failure"}"))
                  return
                }
                case "pdel" => {
                  val removed = player.removePerm(args(3))
                  sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.player.delPerm.${if (removed) "success" else "failure"}"))
                  return
                }
                case "gadd" => {
                  val added = player.addToGroup(args(3))
                  sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.player.addToGroup.${if (added) "success" else "failure"}"))
                  return
                }
                case "gdel" => {
                  val removed = player.removeFromGroup(args(3))
                  sender.addChatMessage(new ChatComponentServerTranslation(s"msg.proton.player.removeFromGroup.${if (removed) "success" else "failure"}"))
                  return
                }
                case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
                  return
              }
            })
            sender.addChatMessage(new ChatComponentServerTranslation("msg.proton.player.notFound", args(1)))
          }
          case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
        }
      }
      case _ => sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)))
    }
  }

  override def addTabCompletionOptions(sender: ICommandSender, args: Array[String]): util.List[_] = {
    args.length match {
      case 1 => CommandBase.getListOfStringsMatchingLastWord(args, Array("group", "player", "listgroups", "listperms"): _*)
      case 2 => CommandBase.getListOfStringsMatchingLastWord(args, (args(0) match {
        case "group" => "create" :: ProtonSavedData.get.getGroups.map(g => g.getName).toList
        case "player" => MinecraftServer.getServer.getAllUsernames.toList
        case _ => Nil
      }): _*)
      case 3 => CommandBase.getListOfStringsMatchingLastWord(args, (args(0) match {
        case "group" => {
          args(1) match {
            case g if (ProtonSavedData.get.getGroup(g).isDefined) => Array("inherit", "create", "padd", "pdel", "listperms")
            case _ => Array("")
          }
        }
        case "player" => {
          args(1) match {
            case p if (MinecraftServer.getServer.getAllUsernames.contains(p)) => Array("padd", "pdel", "gadd", "gdel", "listperms", "listgroups")
            case _ => Array("")
          }
        }
        case _ => Array("")
      }): _*)
      case 4 => CommandBase.getListOfStringsMatchingLastWord(args, (args(0) match {
        case "group" => {
          args(2) match {
            case _@("inherit" | "create") => ProtonSavedData.get.getGroups.map(g => g.getName).filter(s => !s.equals(args(1)))
            case _@("padd" | "pdel") => ProtonSavedData.get.getPermNodes.map(p => p.getName)
            case _ => Array("")
          }
        }
        case "player" => {
          args(2) match {
            case _@("gadd" | "gdel") => ProtonSavedData.get.getGroups.map(g => g.getName).filter(s => !s.equals(args(1)))
            case _@("padd" | "pdel") => ProtonSavedData.get.getPermNodes.map(p => p.getName)
            case _ => Array("")
          }
        }
      }): _*)
      case _ => ImmutableList.of()
    }
  }
}
