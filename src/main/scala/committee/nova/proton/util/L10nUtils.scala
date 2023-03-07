package committee.nova.proton.util

import committee.nova.proton.Proton
import committee.nova.proton.config.ServerConfig
import committee.nova.sjl10n.L10nUtilitiesJ
import committee.nova.sjl10n.L10nUtilitiesJ.JsonText

import java.text.MessageFormat
import scala.collection.mutable

object L10nUtils {
  val l10nMap: mutable.Map[String, JsonText] = new mutable.HashMap[String, JsonText]()

  def getFromCurrentLang(key: String): String = getL10n(ServerConfig.getLanguage).get(key)

  def getFromCurrentLang(key: String, args: Any*): String = MessageFormat.format(getFromCurrentLang(key), args.toArray.asInstanceOf[Array[AnyRef]].toSeq: _*)

  def getL10n(lang: String): JsonText = {
    l10nMap.foreach(m => if (lang == m._1) return m._2)
    val n = L10nUtilitiesJ.create(Proton.MODID, lang)
    l10nMap.put(lang, n)
    n
  }

  def initializeL10n(lang: String): JsonText = {
    if (lang != "en_us") getL10n(lang)
    getL10n("en_us")
  }
}
