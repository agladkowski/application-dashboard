import model.DashboardModel.{WebPageResponse, Environment, ApplicationStatus}
import org.junit.Test
import service.DowntimeHistoryService

/**
 * Created by andrzej on 24/10/2015.
 */
class DowntimeHistoryServiceTest {



  @Test
  def testRecordDowntimeHistory = {
    // prepare
    val environment: Environment = Environment("int", "http://localhost:9000/status", true)
    val statusPageResponse: WebPageResponse = WebPageResponse("", 200, None)
    val version: Some[String] = Some("1.0.0-SNAPSHOT")
    val host: Some[String] = Some("sercice1.com")
    val status: ApplicationStatus = ApplicationStatus(null, environment, version, host,statusPageResponse)

    // act
    DowntimeHistoryService.recordHistory(status, "empty")

    // asert
    // check file system that the value was recorded
  }
}
