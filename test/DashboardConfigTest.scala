import model.DashboardModel
import model.DashboardModel._
import org.junit.Assert._
import org.junit.Test

class DashboardConfigTest {

  @Test
  def loadDashboardConfiguration(): Unit = {
    val dashboard = DashboardModel.instance

    val projects: List[Application] = dashboard.applications.toList
    assertEquals(2, projects.size)

    val project = projects.head
    assertEquals("bips", project.name)
    assertEquals("\"version\"\\:\"([0-9\\.\\-_A-Z]*)\"", project.statusPageVersionRegex)

    val environments = project.environments
    assertEquals(2, environments.size)

    val environment = environments.head
    assertEquals("int", environment.name)
    assertEquals("http://localhost:9000/status", environment.statusPageUrl)
  }

}