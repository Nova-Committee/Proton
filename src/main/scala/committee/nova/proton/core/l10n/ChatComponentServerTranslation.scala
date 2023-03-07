package committee.nova.proton.core.l10n

import committee.nova.proton.util.L10nUtils
import net.minecraft.util.text.TextComponentString

import java.text.MessageFormat

class ChatComponentServerTranslation(key: String, args: Any*)
  extends TextComponentString(MessageFormat.format(L10nUtils.getFromCurrentLang(key), args.toArray.asInstanceOf[Array[AnyRef]].toSeq: _*))
