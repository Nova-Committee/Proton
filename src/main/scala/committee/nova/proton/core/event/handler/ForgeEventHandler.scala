package committee.nova.proton.core.event.handler

import committee.nova.proton.Proton
import committee.nova.proton.core.event.handler.FMLEventHandler.{testNode1, testNode2}
import committee.nova.proton.core.event.handler.ForgeEventHandler.isInDevEnv
import committee.nova.proton.core.event.impl.{ProtonGroupInitializationEvent, ProtonPermNodeInitializationEvent, ProtonPlayerInitializationEvent}
import committee.nova.proton.core.perm.Group
import committee.nova.proton.core.player.storage.ProtonEEP
import committee.nova.proton.implicits.PlayerImplicit
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.launchwrapper.Launch
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing
import net.minecraftforge.event.entity.player.PlayerEvent

object ForgeEventHandler {
  def init(): Unit = MinecraftForge.EVENT_BUS.register(new ForgeEventHandler)

  def isInDevEnv: Boolean = Launch.blackboard.get("fml.deobfuscatedEnvironment").asInstanceOf[Boolean]
}

class ForgeEventHandler {
  @SubscribeEvent
  def onConstruct(event: EntityConstructing): Unit = {
    if (!event.entity.isInstanceOf[EntityPlayer]) return
    event.entity.registerExtendedProperties(ProtonEEP.id, new ProtonEEP)
  }

  @SubscribeEvent
  def onClone(event: PlayerEvent.Clone): Unit = {
    val oldPlayer = event.original
    val newPlayer = event.entityPlayer
    val tag = new NBTTagCompound
    oldPlayer.getExtendedProperties(ProtonEEP.id).saveNBTData(tag)
    newPlayer.getExtendedProperties(ProtonEEP.id).loadNBTData(tag)
  }

  @SubscribeEvent
  def onPermNodeInit(e: ProtonPermNodeInitializationEvent): Unit = {
    if (!isInDevEnv) return
    Proton.LOGGER.info("DevEnv Perm Init")
    e.addNode(testNode1, testNode2)
  }

  @SubscribeEvent
  def onGroupInit(e: ProtonGroupInitializationEvent): Unit = {
    if (!isInDevEnv) return
    Proton.LOGGER.info("DevEnv Group Init")
    val defaultGroup = Group("ProtonDefault")
    defaultGroup.addPerm(testNode1)
    defaultGroup.addPerm(testNode2)
    e.addGroup(defaultGroup)
  }

  @SubscribeEvent
  def onPlayerInit(e: ProtonPlayerInitializationEvent): Unit = {
    if (!isInDevEnv) return
    Proton.LOGGER.info("DevEnv Player Init")
    e.addFunc(p => p.addToGroup("ProtonDefault"))
  }
}
