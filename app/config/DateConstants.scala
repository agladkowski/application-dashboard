package config

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

/**
 * Created by andrzej on 27/10/2015.
 */
object DateConstants {
  val dateFormatString: String = "yyyy-MM-dd_HH.mm.ss.SSS"

  val uiDateFormatString: String = "yyyy-MM-dd HH:mm:ss"

  val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateFormatString)
}
