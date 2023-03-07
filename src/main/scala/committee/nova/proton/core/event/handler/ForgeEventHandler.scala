package committee.nova.proton.core.event.handler

import committee.nova.proton.Proton
import committee.nova.proton.Proton.protonCapability
import committee.nova.proton.core.event.handler.ForgeEventHandler._
import committee.nova.proton.core.event.impl.{ProtonImmutableGroupInitializationEvent, ProtonPermNodeInitializationEvent, ProtonPlayerInitializationEvent}
import committee.nova.proton.core.perm.{Group, PermNode}
import committee.nova.proton.core.player.storage.impl.ProtonCapability
import committee.nova.proton.implicits.PlayerImplicit
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.launchwrapper.Launch
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
  def onAttachCaps(event: AttachCapabilitiesEvent[Entity]): Unit = {
    event.getObject match {
      case _: EntityPlayer =>
        event.addCapability(new ResourceLocation(Proton.MODID, Proton.MODID), new ProtonCapability.Provider)
      case _ =>
    }
  }

  @SubscribeEvent
  def onClone(event: PlayerEvent.Clone): Unit = {
    val oldPlayer = event.getOriginal
    val newPlayer = event.getEntityPlayer
    val cap = protonCapability
    val storage = cap.getStorage
    if (!(oldPlayer.hasCapability(cap, null) && newPlayer.hasCapability(cap, null))) return
    storage.readNBT(cap, newPlayer.getCapability(cap, null), null, storage.writeNBT(cap, oldPlayer.getCapability(cap, null), null))
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
