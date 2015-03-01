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
 */
object DashboardConfig {
  val dashboardConfigName: String = "dashboard.json"
  val dashboardConfigHome: String = System.getProperty("user.home") + File.separator + ".dashboard-home" + File.separator
  val logger: slf4j.Logger = LoggerFactory.getLogger(DashboardConfig.getClass)

  case class Config(dashboardJsonLocation: String, dashboardJson: String) {
    def isEmbedded: Boolean = {
      dashboardJsonLocation.startsWith("embedded")
    }
  }

  case class ConfigHistoryItem(name: String, lastModified: DateTime, filePath: String) {
    def isCurrent: Boolean = {
      return name.equals(dashboardConfigName)
    }
  }

  // ---------------------------------------------------------------------------------

  def get(): Config = {
    val dashboardConfigOverride: Option[Config] = loadConfigFromFile(Option(dashboardConfigHome + dashboardConfigName))
    if (dashboardConfigOverride.isDefined) {
      return dashboardConfigOverride.get
    }

    val externalDashboardConfig: Option[Config] = loadConfigFromFile(Play.configuration.getString("dashboard.config.location"))
    if (externalDashboardConfig.isDefined) {
      return externalDashboardConfig.get
    }

    logger.info("Loading default dashboard.json")

    val inputJsonStream: InputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(dashboardConfigName)
    val dashboardJson = scala.io.Source.fromInputStream(inputJsonStream).mkString
    Config("embedded:dashboard.json", dashboardJson)
  }

  def updateConfig(newConfigFileContent: String) = {
    archiveCurrentConfig()
    val newConfigFileLocation = dashboardConfigHome + dashboardConfigName
    FileUtils.save(newConfigFileLocation, newConfigFileContent)
  }

  def deleteHistoryConfig(configName: Option[String]) = {
    if (configName.isDefined && !dashboardConfigName.equals(configName)) { // making sure it's not current config
      FileUtils.delete(dashboardConfigHome + configName.get)
    }
  }

  def restoreConfig(configName: Option[String]) = {
    if (configName.isDefined && !dashboardConfigName.equals(configName)) { // making sure it's not current config
      val configToRestore: String = scala.io.Source.fromFile(dashboardConfigHome + configName.get).mkString
      updateConfig(configToRestore)
    }
  }

  def getHistory(): Array[ConfigHistoryItem] = {
    val dashboardConfigs: Array[File] = FileUtils.listFiles(dashboardConfigHome)
    dashboardConfigs.map { file =>
      ConfigHistoryItem(file.getName, new DateTime(file.lastModified()), file.getAbsolutePath)
    }
  }

  def viewConfig(configName: Option[String]) = {
    scala.io.Source.fromFile(dashboardConfigHome + configName.get).mkString
  }

  // ----------------------------------------------------------------------------------

  def loadConfigFromFile(dashboardConfigFile: Option[String]): Option[Config] = {
    try {
      if (dashboardConfigFile.isDefined) {
        val source: BufferedSource = Source.fromFile(dashboardConfigFile.get)
        val dashboardJson = source.mkString
        source.close()
        logger.info(s"Loading configuration from ${dashboardConfigFile}")
        return Some(Config(dashboardConfigFile.get, dashboardJson))
      }
    } catch {
      case e: Exception => logger.info(s"Unable to load configuration from ${dashboardConfigFile}")
    }
    None
  }

  def archiveCurrentConfig() = {
    val currentConfig: Config = get()
    val dashboardConfigToArchive: String = currentConfig.dashboardJson
    val archiveConfigFile: String = generateArchiveFileName
    FileUtils.save(archiveConfigFile, dashboardConfigToArchive)
  }

  def generateArchiveFileName: String = {
    val currentDate = new DateTime().toString("yyyy-MM-dd_hh.mm.ss.sss")
    s"${dashboardConfigHome}${dashboardConfigName}.${currentDate}"
  }
}
