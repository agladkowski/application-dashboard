package service

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import config.{DateConstants, DashboardConfig}
import model.DashboardModel.ApplicationStatus
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, Days}
import org.slf4j
import org.slf4j.LoggerFactory
import play.Logger
import utils.FileUtils

/**
  * Created by andrzej on 01/04/2015.
  * References:
  * Timeline graph library: https://google-developers.appspot.com/chart/interactive/docs/gallery/timeline
  *
  */
object DowntimeHistoryService {
   val logger: slf4j.Logger = LoggerFactory.getLogger("DowntimeHistoryService")

  case class ApplicationDowntime(id: Int = 0, applicationName: String, environment: String, start: DateTime, end: Option[DateTime]){
    def hasStarted = end.isEmpty
    def hasEnded = end.isDefined
    def downtimeFileName = {
      if (end.isEmpty) {
        throw new IllegalStateException("downtimeEnd not defined!")
      }
      start.toString(DateConstants.dateFormatString) + "__" + end.get.toString(DateConstants.dateFormatString)
    }
    def startDate = start.toString(config.DateConstants.uiDateFormatString)
    def endDate = end.getOrElse(new DateTime()).toString(config.DateConstants.uiDateFormatString)
  }

  def getDowntimeHomeDir(): String = {
    s"${DashboardConfig.getDashboardHome}history/downtime"
  }

  def recordDowntime(status: ApplicationStatus, downtimeHomeDir: String = getDowntimeHomeDir()): Option[ApplicationDowntime] = {
   val downtimeFile: File = new File(downtimeHomeDir + "/" + status.application.name + "/" + status.environment.name + "/downtime")

   if (status.isVersionDefined && downtimeFile.exists) {// end of downtime
     val downtimeStartStr: String = FileUtils.readFile(downtimeFile)
     val downtimeStart = DateConstants.dateFormatter.parseDateTime(downtimeStartStr)
     val downtimeEnd: Some[DateTime] = Some(new DateTime())
     val downtime: ApplicationDowntime = new ApplicationDowntime(0, status.application.name, status.environment.name, downtimeStart, downtimeEnd)

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
     return Some(new ApplicationDowntime(0, status.application.name, status.environment.name, downtimeStart, None))
   }

   if (!status.isVersionDefined && downtimeFile.exists) {// downtime in progress
     val downtimeStartStr: String = FileUtils.readFile(downtimeFile)
     val downtimeStart = DateConstants.dateFormatter.parseDateTime(downtimeStartStr)
     return Some(new ApplicationDowntime(0, status.application.name, status.environment.name, downtimeStart, None))
   }

   None// no downtime
  }

  def getDowntimeStartAndEnd(file: File): (DateTime, Option[DateTime]) = {
    if (file.getName.startsWith("downtime")) {
      val downtimeStartStr: String = FileUtils.readFile(file)
      val startDate = DateConstants.dateFormatter.parseDateTime(downtimeStartStr)
      return (startDate, None)// downtime file with recorded start time
    }

    val dateAndVersionTuple: Array[String] = file.getName.split("__")
    val startDate = DateConstants.dateFormatter.parseDateTime(dateAndVersionTuple(0))
    val endDate = DateConstants.dateFormatter.parseDateTime(dateAndVersionTuple(1))
    return (startDate, Some(endDate))// valid downtime file [downtimeStart]__[downtimeEnd]
  }

  def downtimeHistory(numberOfDays: Int, downtimeHomeDir: String = getDowntimeHomeDir()):Array[ApplicationDowntime] = {
    val idGenerator = new AtomicInteger()
    val applications: Array[File] = new File(downtimeHomeDir).listFiles().filter(_.isDirectory)
    val downtimeFiles: Array[ApplicationDowntime] = applications.flatMap { application: File =>
      val environments: Array[File] = application.listFiles().filter(_.isDirectory)
      environments.flatMap { environment: File =>
        val versions: Array[File] = environment.listFiles().filter(_.isFile)
        versions.map { downtimeFile: File =>
          val (start, end): (DateTime, Option[DateTime]) = getDowntimeStartAndEnd(downtimeFile)
          new ApplicationDowntime(idGenerator.getAndIncrement, application.getName, environment.getName, start, end)
        }
      }
    }

    val dateFilter: DateTime = new DateTime().minusDays(numberOfDays)
    val downtimeHistoryToReturn: Array[ApplicationDowntime] = downtimeFiles.filter(_.start.isAfter(dateFilter))

    Logger.info("DowntimeHistory size = " + downtimeHistoryToReturn.length)

    downtimeHistoryToReturn
  }

}
