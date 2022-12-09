package committee.nova.proton

import committee.nova.proton.config.ServerConfig
import committee.nova.proton.core.command.init.CommandInit
import committee.nova.proton.core.event.handler.{FMLEventHandler, ForgeEventHandler}
import committee.nova.proton.core.event.impl.{ProtonImmutableGroupInitializationEvent, ProtonPermNodeInitializationEvent, ProtonPlayerInitializationEvent}
import committee.nova.proton.util.L10nUtils
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent, FMLServerStartingEvent}
import cpw.mods.fml.relauncher.Side
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Logger

@Mod(modid = Proton.MODID, useMetadata = true, modLanguage = "scala")
object Proton {
  var LOGGER: Logger = _
  final val MODID = "proton"

  @EventHandler def preInit(e: FMLPreInitializationEvent): Unit = {
    LOGGER = e.getModLog
    ServerConfig.init(e)
    val initialLang = L10nUtils.initializeL10n(ServerConfig.getLanguage)
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

  @EventHandler def serverStarting(e: FMLServerStartingEvent): Unit = {
    CommandInit.init(e)
  }
}
