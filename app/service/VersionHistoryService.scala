package service

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import config.{DateConstants, DashboardConfig}
import model.DashboardModel.ApplicationStatus
import org.joda.time.DateTime
import play.Logger
import utils.FileUtils

/**
 * Created by andrzej on 01/04/2015.
 */
object VersionHistoryService {
  val getHistoryHomeDir = s"${DashboardConfig.getDashboardHome}history/versions"

  case class VersionHistory(id: Int, applicationName: String, environment: String, version: String, date: DateTime) {
    def eventDate = date.toString(config.DateConstants.uiDateFormatString)
  }

  def recordHistory(status: ApplicationStatus): Unit = {
    val deploymentHistoryDir: String = s"${getHistoryHomeDir}/${status.application.name}/${status.environment.name}"
    if (status.isVersionDefined) {
      val versionHistoryFiles: Array[File] = FileUtils.listFiles(deploymentHistoryDir).filter(getLatestRecordedVersion(_).isDefined)

      if (versionHistoryFiles.length == 0) {
        Logger.debug(s"Version of ${status.application.name}/${status.environment.name} changed to ${status.version.get}")
        recordVersionChange(status, deploymentHistoryDir)
      } else {
        val lastRecordedVersionFile: File = versionHistoryFiles.sortBy(_.lastModified()).last
        val latestRecordedVersion: Option[RecordedVersion] = getLatestRecordedVersion(lastRecordedVersionFile)
        val versionHasChanged: Boolean = latestRecordedVersion.isDefined &&  !latestRecordedVersion.get.version.contains(status.version.getOrElse(""))
        if (versionHasChanged) {
          Logger.debug(s"Version of ${status.application.name}/${status.environment.name} changed from ${latestRecordedVersion.get.version} to ${status.version.get}")
          recordVersionChange(status, deploymentHistoryDir)
        }
      }
    }
  }

  case class RecordedVersion(file: File, version: String, date: DateTime)

  def getLatestRecordedVersion(file: File): Option[RecordedVersion] = {
    val dateAndVersionTuple: Array[String] = file.getName.split("__")
    val date = DateConstants.dateFormatter.parseDateTime(dateAndVersionTuple(0))
    if (dateAndVersionTuple.length == 2) {
      val version: String = dateAndVersionTuple(1)
      return Some(RecordedVersion(file, version, date))
    }

    None
  }

  def recordVersionChange(status: ApplicationStatus, deploymentHistoryDir: String): Unit = {
    if (!status.isVersionDefined) {
      return // do not record any error statuses
    }
    val currentDate: String = new DateTime().toString(DateConstants.dateFormatString)
    val historyFileName = s"${deploymentHistoryDir}/${currentDate}__${status.version.getOrElse("")}"
    FileUtils.createNewFile(historyFileName)
  }

  def versionHistory(numberOfDays: Int):Array[VersionHistory] = {
    val idGenerator = new AtomicInteger()
    val applications: Array[File] = new File(getHistoryHomeDir).listFiles().filter(_.isDirectory)
    val historyList: Array[VersionHistory] = applications.flatMap { application: File =>
      val environments: Array[File] = application.listFiles().filter(_.isDirectory)
      environments.flatMap { environment: File =>
        val versions: Array[File] = environment
          .listFiles()
          .filter(_.isFile)
          .filter(getLatestRecordedVersion(_) != None) // file contains valid [date]__[version] information

        versions.map { versionFile: File =>
          val latestRecordedVersion: Option[RecordedVersion] = getLatestRecordedVersion(versionFile)
          new VersionHistory(idGenerator.getAndIncrement, application.getName, environment.getName, latestRecordedVersion.get.version, latestRecordedVersion.get.date)
        }
      }
    }

    val dateFilter: DateTime = new DateTime().minusDays(numberOfDays)
    val historyToReturn: Array[VersionHistory] = historyList.filter(_.date.isAfter(dateFilter))

    Logger.info("versionHistory size = " + historyToReturn.length)

    historyToReturn
  }

}
