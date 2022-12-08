package committee.nova.proton.core.l10n

import committee.nova.proton.util.L10nUtils
import net.minecraft.util.ChatComponentText

import java.text.MessageFormat

class ChatComponentServerTranslation(key: String, args: Any*)
  extends ChatComponentText(MessageFormat.format(L10nUtils.getFromCurrentLang(key), args.toArray.asInstanceOf[Array[AnyRef]].toSeq: _*))
