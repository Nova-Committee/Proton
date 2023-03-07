package committee.nova.proton.core.command.init

import committee.nova.proton.core.command.impl.CommandProton
import net.minecraftforge.fml.common.event.FMLServerStartingEvent

object CommandInit {
  def init(e: FMLServerStartingEvent): Unit = e.registerServerCommand(CommandProton.instance)
}
