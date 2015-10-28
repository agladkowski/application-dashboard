package service

import java.io.File

import config.{DateConstants, DashboardConfig}
import model.DashboardModel.ApplicationStatus
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, Days}
import org.slf4j
import org.slf4j.LoggerFactory
import utils.FileUtils

/**
  * Created by andrzej on 01/04/2015.
  * References:
  * Timeline graph library: https://google-developers.appspot.com/chart/interactive/docs/gallery/timeline
  *
  */
object DowntimeHistoryService {
   val logger: slf4j.Logger = LoggerFactory.getLogger("DowntimeHistoryService")

   case class DowntimeHistory(id: Int = 0, applicationName: String, environment: String, date: DateTime) {
     def eventDate = date.toString(DateConstants.uiDateFormatString)
   }

  case class ApplicationDowntime(applicationName: String, environment: String, downtimeStart: DateTime, downtimeEnd: Option[DateTime]){
    def hasStarted = downtimeEnd.isEmpty
    def hasEnded = downtimeEnd.isDefined
    def downtimeFileName = {
      if (downtimeEnd.isEmpty) {
        throw new IllegalStateException("downtimeEnd not defined!")
      }
      downtimeStart.toString(DateConstants.dateFormatString) + "__" + downtimeEnd.get.toString(DateConstants.dateFormatString)
    }
  }

   def recordDowntime(status: ApplicationStatus, homeDir: String = s"${DashboardConfig.getDashboardHome}history/downtime"): Option[ApplicationDowntime] = {
     val downtimeFile: File = new File(homeDir + "/" + status.application.name + "/" + status.environment.name + "/downtime")

     if (status.isVersionDefined && downtimeFile.exists) {// end of downtime
       val downtimeStartStr: String = FileUtils.readFile(downtimeFile)
       val downtimeStart = DateConstants.dateFormatter.parseDateTime(downtimeStartStr)
       val downtimeEnd: Some[DateTime] = Some(new DateTime())
       val downtime: ApplicationDowntime = new ApplicationDowntime(status.application.name, status.environment.name, downtimeStart, downtimeEnd)

       FileUtils.delete(downtimeFile.getAbsolutePath)// deleting downtime file, IMPORTANT!
       FileUtils.createNewFile(new File(downtimeFile.getParent, downtime.downtimeFileName), "")// creating downtime duration file e.g. 2015-05-24_10.00.00.048__2015-05-24_11.00.00.048

       logger.info(s"Downtime end ${status.application.name}/${status.environment.name} duration[days] " + Days.daysBetween(downtimeStart, downtimeEnd.get))
       return Some(downtime)
     }

     if (!status.isVersionDefined && !downtimeFile.exists) {// downtime start
       val downtimeStart: DateTime = new DateTime()
       val downtimeStartStr: String = downtimeStart.toString(DateConstants.dateFormatString)

       FileUtils.createNewFile(downtimeFile, downtimeStartStr)// creating downtime file with current date

       logger.info(s"Downtime start ${status.application.name}/${status.environment.name} ${downtimeStart}")
       return Some(new ApplicationDowntime(status.application.name, status.environment.name, downtimeStart, None))
     }

     if (!status.isVersionDefined && downtimeFile.exists) {// downtime in progress
       val downtimeStartStr: String = FileUtils.readFile(downtimeFile)
       val downtimeStart = DateConstants.dateFormatter.parseDateTime(downtimeStartStr)
       return Some(new ApplicationDowntime(status.application.name, status.environment.name, downtimeStart, None))
     }

     None// no downtime
   }

 }
