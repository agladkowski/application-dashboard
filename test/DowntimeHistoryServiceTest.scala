import java.io.File

import config.DateConstants
import model.DashboardModel.{Application, ApplicationStatus, Environment, WebPageResponse}
import org.joda.time.DateTime
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.rules.TemporaryFolder
import org.junit.{After, Before, Test}
import service.DowntimeHistoryService
import service.DowntimeHistoryService.ApplicationDowntime
import utils.FileUtils

/**
 * Created by andrzej on 24/10/2015.
 */
class DowntimeHistoryServiceTest {
  val APPLICATION_VERSION: String = "1.0.0-SNAPSHOT"
  val APPLICATION_NAME: String = "Service1"
  val ENVIRONMENT_NAME: String = "int"
  val APPLICATION_HOST: String = "service1.com"
  val STATUS_PAGE_URL: String = "http://localhost:9000/status"

  val tmpFile: TemporaryFolder = new TemporaryFolder()

  @Before
  def setup = {
    tmpFile.create()
  }

  @After
  def tearDown = {
    tmpFile.delete()
  }

  def createApplicationStatus(version: Option[String]): ApplicationStatus = {
    val application: Application = Application(name=APPLICATION_NAME, statusPageVersionRegex="statusPageRegex", hostRegex="hostRegex",environments=null)
    val environment: Environment = Environment(ENVIRONMENT_NAME, STATUS_PAGE_URL, true)
    val statusPageResponse: WebPageResponse = WebPageResponse("Status page content...", 200, None)
    val host: Some[String] = Some(APPLICATION_HOST)
    return ApplicationStatus(application, environment, version, host,statusPageResponse)
  }

  @Test
  def testRecordStartOfDowntime = {
    // prepare
    val application: ApplicationStatus = createApplicationStatus(None)
    val downtimeHistoryDir: String = tmpFile.getRoot.getAbsolutePath
    val expectedDowntimeFile: String = downtimeHistoryDir + "/" + APPLICATION_NAME + "/" + ENVIRONMENT_NAME + "/downtime"

    assertFalse("Expecting no downtime file", new File(expectedDowntimeFile).exists())

    // act
    val applicationDowntime: Option[ApplicationDowntime] = DowntimeHistoryService.recordDowntime(application, downtimeHistoryDir)

    // assert
    assertTrue("Expecting downtime", applicationDowntime.isDefined)
    assertTrue("Expecting downtime file to be created", new File(expectedDowntimeFile).exists())

    val downtimeFileContent: String = FileUtils.readFile(new File(expectedDowntimeFile))
    val downtimeStart: DateTime = DateConstants.dateFormatter.parseDateTime(downtimeFileContent)
    assertTrue(downtimeStart.isBefore(new DateTime()))
  }

  @Test
  def testRecordEndOfDowntime = {
    // prepare
    val applicationDown: ApplicationStatus = createApplicationStatus(None)
    val applicationUp: ApplicationStatus = createApplicationStatus(Some("1.0.0-SNAPSHOT"))
    val downtimeHistoryDir: String = tmpFile.getRoot.getAbsolutePath
    val expectedDowntimeFile: String = downtimeHistoryDir + "/" + APPLICATION_NAME + "/" + ENVIRONMENT_NAME + "/downtime"

    DowntimeHistoryService.recordDowntime(applicationDown, downtimeHistoryDir)
    assertTrue("Expecting downtime file to exist", new File(expectedDowntimeFile).exists())

    // act
    val applicationDowntime : Option[ApplicationDowntime] = DowntimeHistoryService.recordDowntime(applicationUp, downtimeHistoryDir)

    // assert
    assertTrue("Expecting end of downtime", applicationDowntime.isDefined)
    assertFalse("Expecting downtime file to be deleted", new File(expectedDowntimeFile).exists())

    val expectedDowntimeDurationFile: String = downtimeHistoryDir + "/" + APPLICATION_NAME + "/" + ENVIRONMENT_NAME + "/" + applicationDowntime.get.downtimeFileName
    assertTrue("Expecting downtime duration file", new File(expectedDowntimeDurationFile).exists())
  }

  @Test
  def testDowntimeInProgress = {
    // prepare
    val applicationDown: ApplicationStatus = createApplicationStatus(None)
    val downtimeHistoryDir: String = tmpFile.getRoot.getAbsolutePath
    val expectedDowntimeFile: String = downtimeHistoryDir + "/" + APPLICATION_NAME + "/" + ENVIRONMENT_NAME + "/downtime"

    DowntimeHistoryService.recordDowntime(applicationDown, downtimeHistoryDir)
    assertTrue("Expecting downtime file to exist", new File(expectedDowntimeFile).exists())

    // act
    val applicationDowntime : Option[ApplicationDowntime] = DowntimeHistoryService.recordDowntime(applicationDown, downtimeHistoryDir)

    // assert
    assertTrue("Expecting downtime", applicationDowntime.isDefined)
    assertTrue("Expecting downtime file to exist", new File(expectedDowntimeFile).exists())
    assertTrue("Expecting downtime in progress", applicationDowntime.get.downtimeEnd.isEmpty)
  }

  @Test
  def testRecordNoDowntimeWhenAppIsRunning = {
    // prepare
    val application: ApplicationStatus = createApplicationStatus(Some(APPLICATION_VERSION))
    val downtimeHistoryDir: String = tmpFile.getRoot.getAbsolutePath
    val expectedDowntimeFile: String = downtimeHistoryDir + "/" + APPLICATION_NAME + "/" + ENVIRONMENT_NAME + "/downtime"

    assertFalse("Expecting no downtime file", new File(expectedDowntimeFile).exists())

    // act
    val applicationDowntime: Option[ApplicationDowntime] = DowntimeHistoryService.recordDowntime(application, downtimeHistoryDir)

    // assert
    assertTrue("Expecting no downtime", applicationDowntime.isEmpty)
    assertFalse("Expecting no downtime file", new File(expectedDowntimeFile).exists())
  }
}
