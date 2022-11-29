package committee.nova.proton

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(modid = Proton.MODID, useMetadata = true, modLanguage = "scala")
object Proton {
  var LOGGER: Logger = _
  final val MODID = "proton"

  @EventHandler def preInit(e: FMLPreInitializationEvent): Unit = {
    LOGGER = e.getModLog
  }
}
