package config

import java.io.{File, InputStream}

import org.joda.time.DateTime
import org.slf4j
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current
import utils.FileUtils

import scala.io.{BufferedSource, Source}

/**
 * Created by andrzej on 22/02/2015.
 *
 */
object DashboardConfig {
  val dashboardConfigName = "dashboard.json"
  val dashboardConfigHome = System.getProperty("user.home") + File.separator + ".dashboard-home" + File.separator

  val logger = LoggerFactory.getLogger(DashboardConfig.getClass)

  case class Config(dashboardJsonLocation: String, dashboardJson: String) {
    val isEmbedded: Boolean = {
      dashboardJsonLocation.startsWith("embedded")
    }
  }

  case class ConfigHistoryItem(name: String, lastModified: DateTime, filePath: String) {
    val isCurrent: Boolean = {
      name == dashboardConfigName
    }
  }

  // ---------------------------------------------------------------------------------

  def get(): Config = {
    loadConfigFromFile(Some(dashboardConfigHome + dashboardConfigName)).orElse {
      loadConfigFromFile(Play.configuration.getString("dashboard.config.location"))
    }.getOrElse {
      logger.info("Loading default dashboard.json")
      val inputJsonStream: InputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(dashboardConfigName)
      val dashboardJson = scala.io.Source.fromInputStream(inputJsonStream).mkString
      Config("embedded:dashboard.json", dashboardJson)
    }
  }

  def getHistory: Array[ConfigHistoryItem] = {
    val dashboardConfigs: Array[File] = FileUtils.listFiles(dashboardConfigHome)
    dashboardConfigs.map { file =>
      ConfigHistoryItem(file.getName, new DateTime(file.lastModified()), file.getAbsolutePath)
    }
  }

  def viewConfig(configName: String) = {
    readLines(dashboardConfigHome + configName)
  }

  def updateConfig(newConfigFileContent: String) = {
    archiveCurrentConfig()
    val newConfigFileLocation = dashboardConfigHome + dashboardConfigName
    FileUtils.save(newConfigFileLocation, newConfigFileContent)
  }

  def deleteHistoryConfig(configName: Option[String]) = {
    configName
      .filter( path => dashboardConfigName != path) // making sure it's not current config
      .map{ path =>
        FileUtils.delete(dashboardConfigHome + path)
      }
  }

  def restoreConfig(configName: Option[String]) = {
    configName
      .filter( path => dashboardConfigName != path) // making sure it's not current config
      .map{ path =>
        val configToRestore = readLines(dashboardConfigHome + path)
        updateConfig(configToRestore)
      }
  }

  private def readLines(path: String): String = {
    val source = Source.fromFile(path)
    val body = source.mkString
    source.close()
    body
  }

  // ----------------------------------------------------------------------------------

  private def loadConfigFromFile(dashboardConfigFile: Option[String]): Option[Config] = {
    dashboardConfigFile
      .filter( path => new File(path).exists()) // check file exists
      .map{ path =>
        val dashboardJson = readLines(path)
        logger.info(s"Loading configuration from $dashboardConfigFile")
        Config(dashboardConfigFile.get, dashboardJson)
      }
  }

  private def archiveCurrentConfig() = {
    val currentConfig: Config = get()
    val dashboardConfigToArchive: String = currentConfig.dashboardJson
    val archiveConfigFile: String = generateArchiveFileName
    FileUtils.save(archiveConfigFile, dashboardConfigToArchive)
  }

  private def generateArchiveFileName: String = {
    val currentDate = new DateTime().toString("yyyy-MM-dd_hh.mm.ss.sss")
    s"$dashboardConfigHome$dashboardConfigName.$currentDate"
  }

}
