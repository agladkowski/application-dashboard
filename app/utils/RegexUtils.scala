package utils

import scala.util.matching.Regex

/**
 * Created by andrzej on 14/02/2015.
 */
object RegexUtils {
  def findMatch(pattern: Regex, content: String): String = {
    val result = pattern.findAllIn(content).matchData.map {
      m => if (m.groupCount == 1) m.group(1) else ""
    }

    if (result.isEmpty) return ""

    result.next()
  }
}
