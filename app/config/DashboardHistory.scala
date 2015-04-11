package config

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import model.DashboardModel.ApplicationStatus
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import play.Logger
import utils.FileUtils

/**
 * Created by andrzej on 01/04/2015.
 */
object DashboardHistory {
  val dateFormatString: String = "yyyy-MM-dd_hh.mm.ss.sss"

  val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateFormatString)

  val getHistoryHomeDir = s"${DashboardConfig.getDashboardHome}history/versions"

  case class VersionHistory(id: Int, applicationName: String, environment: String, version: String, date: DateTime) {
    def eventDate = date.toString("yyyy-MM-dd hh:mm:ss")
  }

  def recordHistory(status: ApplicationStatus): Unit = {
    val deploymentHistoryDir: String = s"${getHistoryHomeDir}/${status.application.name}/${status.environment.name}"
    if (!status.error) {
      val versionHistoryFiles: Array[File] = FileUtils.listFiles(deploymentHistoryDir)

      if (versionHistoryFiles.length == 0) {
        recordVersionChange(status, deploymentHistoryDir)
      } else {
        val lastRecordedVersionFile: File = versionHistoryFiles.sortBy(_.getName).last
        val latestRecordedVersion: String = getLatestRecordedVersion(lastRecordedVersionFile)._2
        val versionHasChanged: Boolean = status.version.isDefined && !status.version.getOrElse("").contains(latestRecordedVersion)
        if (versionHasChanged) {
          Logger.debug(s"Version changed ${status.environment.name}/${status.application.name} current ${status.version} latest ${latestRecordedVersion}")
          recordVersionChange(status, deploymentHistoryDir)
        }
      }
    }
  }
  
  def getLatestRecordedVersion(file: File): (DateTime, String) = {
    val dateAndVersionTuple: Array[String] = file.getName.split("__")
    val dateStr: String = dateAndVersionTuple(0)
    val date = dateFormatter.parseDateTime(dateStr)
    if (dateAndVersionTuple.length == 1) {
      return (date, "???")
    }
    val version: String = dateAndVersionTuple(1)
    (date, version)
  }

  def recordVersionChange(status: ApplicationStatus, deploymentHistoryDir: String): Unit = {
    val currentDate: String = new DateTime().toString(dateFormatString)
    val historyFileName = s"${deploymentHistoryDir}/${currentDate}__${status.version.getOrElse("")}"
    if (status.version.isDefined) {
      FileUtils.createNewFile(historyFileName)
    }
  }

  def versionHistory(numberOfDays: Int):Array[VersionHistory] = {
    val idGenerator = new AtomicInteger()
    val applications: Array[File] = new File(getHistoryHomeDir).listFiles().filter(_.isDirectory)
    val historyList: Array[VersionHistory] = applications.map { application: File =>
      val environments: Array[File] = new File(getHistoryHomeDir + "/" + application.getName).listFiles().filter(_.isDirectory)
      environments.map{ environment: File =>
        val versions: Array[File] = new File(getHistoryHomeDir + "/" + application.getName + "/" + environment.getName).listFiles().filter(_.isFile)
        versions.map{versionFile: File =>
          val latestRecordedVersion: (DateTime, String) = getLatestRecordedVersion(versionFile)
          new VersionHistory(idGenerator.getAndIncrement, application.getName, environment.getName, latestRecordedVersion._2, latestRecordedVersion._1)
        }
      }.flatten
    }.flatten

    val dateFilter: DateTime = new DateTime().minusDays(numberOfDays)
    val historyToReturn: Array[VersionHistory] = historyList.filter(_.date.isAfter(dateFilter))

    Logger.info("versionHistory size = " + historyToReturn.length)

    historyToReturn
  }

}
