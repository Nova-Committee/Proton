package committee.nova.proton

import committee.nova.proton.config.ServerConfig
import committee.nova.proton.core.command.init.CommandInit
import committee.nova.proton.core.event.handler.{FMLEventHandler, ForgeEventHandler}
import committee.nova.proton.core.event.impl.{ProtonImmutableGroupInitializationEvent, ProtonPermNodeInitializationEvent, ProtonPlayerInitializationEvent}
import committee.nova.proton.core.player.storage.api.IProtonCapability
import committee.nova.proton.core.player.storage.impl.ProtonCapability
import committee.nova.proton.core.server.storage.ProtonSavedData
import committee.nova.proton.util.L10nUtils
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.{Capability, CapabilityInject, CapabilityManager}
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event._
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Logger

import java.util.concurrent.Callable

@Mod(modid = Proton.MODID, useMetadata = true, modLanguage = "scala", acceptableRemoteVersions = "*")
object Proton {
  var LOGGER: Logger = _
  final val MODID = "proton"

  var protonCapability: Capability[IProtonCapability] = _

  @CapabilityInject(classOf[IProtonCapability])
  def setProtonCapability(cap: Capability[IProtonCapability]): Unit = protonCapability = cap

  @EventHandler def preInit(e: FMLPreInitializationEvent): Unit = {
    LOGGER = e.getModLog
    ServerConfig.init(e)
    val initialLang = L10nUtils.initializeL10n(ServerConfig.getLanguage)
    CapabilityManager.INSTANCE.register(classOf[IProtonCapability], new ProtonCapability.Storage, new Callable[IProtonCapability] {
      override def call(): IProtonCapability = new ProtonCapability.Impl
    })
    if (e.getSide == Side.CLIENT) LOGGER.warn(initialLang.get("msg.proton.warn.clientInit"))
    else Array(
      "$$$$$$$\\                       $$\\                         ",
      "$$  __$$\\                      $$ |                        ",
      "$$ |  $$ | $$$$$$\\   $$$$$$\\ $$$$$$\\    $$$$$$\\  $$$$$$$\\  ",
      "$$$$$$$  |$$  __$$\\ $$  __$$\\\\_$$  _|  $$  __$$\\ $$  __$$\\ ",
      "$$  ____/ $$ |  \\__|$$ /  $$ | $$ |    $$ /  $$ |$$ |  $$ |",
      "$$ |      $$ |      $$ |  $$ | $$ |$$\\ $$ |  $$ |$$ |  $$ |",
      "$$ |      $$ |      \\$$$$$$  | \\$$$$  |\\$$$$$$  |$$ |  $$ |",
      "\\__|      \\__|       \\______/   \\____/  \\______/ \\__|  \\__|",
      "Activating Proton..."
    ).foreach(s => LOGGER.info(s))
  }

  @EventHandler def init(e: FMLInitializationEvent): Unit = {
    ForgeEventHandler.init()
    FMLEventHandler.init()
  }

  @EventHandler def postInit(e: FMLPostInitializationEvent): Unit = {
    MinecraftForge.EVENT_BUS.post(new ProtonPermNodeInitializationEvent)
    MinecraftForge.EVENT_BUS.post(new ProtonImmutableGroupInitializationEvent)
    MinecraftForge.EVENT_BUS.post(new ProtonPlayerInitializationEvent)
  }

  @EventHandler def serverStarting(e: FMLServerStartingEvent): Unit = CommandInit.init(e)

  @EventHandler def serverStarted(e: FMLServerStartedEvent): Unit = ProtonSavedData.processCache()
}
