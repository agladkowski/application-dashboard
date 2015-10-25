package service

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import config.DashboardConfig
import model.DashboardModel.ApplicationStatus
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.Logger
import utils.FileUtils

/**
  * Created by andrzej on 01/04/2015.
  * References:
  * Timeline graph library: https://google-developers.appspot.com/chart/interactive/docs/gallery/timeline
  *
  */
object DowntimeHistoryService {
   val dateFormatString: String = "yyyy-MM-dd_hh.mm.ss.sss"

   val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateFormatString)

   //val getDowntimeHistoryHomeDir = s"${DashboardConfig.getDashboardHome}history/downtime"

   case class DowntimeHistory(id: Int, applicationName: String, environment: String, version: String, date: DateTime) {
     def eventDate = date.toString("yyyy-MM-dd hh:mm:ss")
   }

   def recordHistory(status: ApplicationStatus, homeDir: String = s"${DashboardConfig.getDashboardHome}history/downtime"): Unit = {
     // todo ...
   }

 }
