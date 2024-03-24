package config

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import utils.FileUtils

import java.io.{File, InputStream}
import scala.io.Source

/**
 * Created by andrzej on 22/02/2015.
 *
 */
object DashboardConfig {
  val defaultDashboardConfigName = "dashboard.json"
  val defaultDashboardHome = System.getProperty("user.home") + File.separator + ".dashboard-home" + File.separator

  val logger = LoggerFactory.getLogger(DashboardConfig.getClass)

  case class Config(dashboardJsonLocation: String, dashboardJson: String) {
    val isEmbedded: Boolean = dashboardJsonLocation.startsWith("embedded")
  }

  case class ConfigHistoryItem(name: String, lastModified: DateTime, filePath: String) {
    val isCurrent: Boolean = {
      name == defaultDashboardConfigName
    }
  }

  // ---------------------------------------------------------------------------------

  def getConfiguration(name: String, defaultValue: String): String = {
    System.getProperty(name, defaultValue)
  }

  def getDashboardHome: String = {
    getConfiguration("dashboard.home", defaultDashboardHome)
  }

  def get(): Config = {
    logger.info("dashboard.home=" + getDashboardHome)
    loadConfigFromFile(Some(getDashboardHome + defaultDashboardConfigName)).orElse {
      val configFile = getConfiguration("dashboard.config.location", null)
      logger.info("Loading dashboard.config.location=" + getConfiguration("dashboard.config.location", null))
      loadConfigFromFile(Some(configFile))
    }.getOrElse {
      logger.info("Loading default dashboard.json")
      val inputJsonStream: InputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(defaultDashboardConfigName)
      val dashboardJson = scala.io.Source.fromInputStream(inputJsonStream).mkString
      Config("embedded:dashboard.json", dashboardJson)
    }
  }

  def getHistory: Array[ConfigHistoryItem] = {
    val dashboardConfigs: Array[File] = FileUtils.listFiles(getDashboardHome)
    dashboardConfigs.map { file =>
      ConfigHistoryItem(file.getName, new DateTime(file.lastModified()), file.getAbsolutePath)
    }
  }

  def viewConfig(configName: String) = {
    readLines(getDashboardHome + configName)
  }

  def updateConfig(newConfigFileContent: String) = {
    archiveCurrentConfig()
    val newConfigFileLocation = getDashboardHome + defaultDashboardConfigName
    logger.info(s"Updating config $newConfigFileLocation")
    FileUtils.save(newConfigFileLocation, newConfigFileContent)
  }

  def deleteHistoryConfig(configName: Option[String]) = {
    configName
      .filter( path => defaultDashboardConfigName != path) // making sure it's not current config
      .map{ path =>
        FileUtils.delete(getDashboardHome + path)
      }
  }

  def restoreConfig(configName: Option[String]) = {
    configName
      .filter( path => defaultDashboardConfigName != path) // making sure it's not current config
      .map{ path =>
        val configToRestore = readLines(getDashboardHome + path)
        logger.info(s"Restoring config $configToRestore")
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
    logger.info(s"Archiving current config to $archiveConfigFile")
    FileUtils.save(archiveConfigFile, dashboardConfigToArchive)
  }

  private def generateArchiveFileName: String = {
    val currentDate = new DateTime().toString(DateConstants.dateFormatString)
    s"$getDashboardHome$defaultDashboardConfigName.$currentDate"
  }

}
