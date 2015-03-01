package utils

/**
 * Created by andrzej on 14/02/2015.
 */
object RegexUtils {
  def findMatch(pattern: String, content: String): String = {
    try {
      val result = pattern.r.findAllIn(content).matchData.map {
        m => if (m.groupCount == 1) m.group(1) else ""
      }

      if (result.isEmpty)
        return ""

      return result.next()
    } catch {
      case e: Exception => s"Invalid: $pattern"
    }
  }
}
