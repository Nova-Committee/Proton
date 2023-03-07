package committee.nova.proton.config

import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

object ServerConfig {
  private var config: Configuration = _
  private var language: String = _

  def getLanguage: String = language

  def init(event: FMLPreInitializationEvent): Unit = {
    config = new Configuration(event.getSuggestedConfigurationFile)
    sync()
  }

  def sync(): Unit = {
    config.load()
    language = config.getString("language", Configuration.CATEGORY_GENERAL, "en_us", "Language ID of the server messages")
    config.save()
  }
}
