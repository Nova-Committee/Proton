package committee.nova.proton.core.event.handler

import committee.nova.proton.Proton
import committee.nova.proton.core.event.handler.ForgeEventHandler._
import committee.nova.proton.core.event.impl.{ProtonImmutableGroupInitializationEvent, ProtonPermNodeInitializationEvent, ProtonPlayerInitializationEvent}
import committee.nova.proton.core.perm.{Group, PermNode}
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
  val testNode0: PermNode = PermNode("proton.test.0")
  val testNode1: PermNode = PermNode("proton.test.1")
  val testNode2: PermNode = PermNode("proton.test.2")
  val testNode3: PermNode = PermNode("proton.test.3")

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
    e.addNode(testNode0, testNode1, testNode2, testNode3)
  }

  @SubscribeEvent
  def onGroupInit(e: ProtonImmutableGroupInitializationEvent): Unit = {
    if (!isInDevEnv) return
    Proton.LOGGER.info("DevEnv Group Init")
    val protonGroup = Group("ProtonDefault")
    protonGroup.addPerm(testNode2)
    protonGroup.addPerm(testNode3)
    e.addGroup(protonGroup)
  }

  @SubscribeEvent
  def onPlayerInit(e: ProtonPlayerInitializationEvent): Unit = {
    if (!isInDevEnv) return
    Proton.LOGGER.info("DevEnv Player Init")
    e.addFunc(p => Array(testNode0, testNode1).foreach(n => p.addPerm(n)))
    e.addFunc(p => p.addToGroup("ProtonDefault"))
  }
}
