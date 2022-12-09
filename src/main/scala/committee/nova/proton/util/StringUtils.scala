package committee.nova.proton.util

object StringUtils {
  def convertIteratorToString[T](iterator: Iterator[T], convertor: (T, Int) => String, defaultValue: String): String = {
    val buffer = new StringBuffer()
    iterator.zipWithIndex.foreach(z => buffer.append(s"${convertor.apply(z._1, z._2)}, "))
    val lastIndex = buffer.lastIndexOf(",")
    if (lastIndex > 0) buffer.delete(lastIndex, buffer.length())
    else return defaultValue
    buffer.toString
  }
}
