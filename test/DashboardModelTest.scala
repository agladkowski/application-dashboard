import model.DashboardModel
import model.DashboardModel._
import org.junit.Assert._
import org.junit.Test

class DashboardModelTest {

  @Test
  def loadDashboardConfiguration(): Unit = {
    // prepare
    val jsonContentStream = Thread.currentThread().getContextClassLoader.getResourceAsStream("dashboard.json")
    val jsonContent = scala.io.Source.fromInputStream(jsonContentStream).mkString

    // act
    val dashboard = DashboardModel.create(jsonContent)

    // assert
    assertEquals("1.1", dashboard.version)

    val projects: List[Application] = dashboard.applications.toList
    assertEquals(5, projects.size)

    val project = projects.head
    assertEquals("Service1", project.name)
    assertEquals("\"version\"\\:\"([0-9\\.\\-_A-Z]*)\"", project.statusPageVersionRegex)

    val environments = project.environments
    assertEquals(3, environments.size)

    val environment = environments.head
    assertEquals("int", environment.name)
    assertEquals("http://localhost:9000/status/Service1/version/3.0.0-SNAPSHOT", environment.statusPageUrl)
    assertEquals("http://localhost:9000", environment.baseUrl)

    val links = project.links
    assertEquals(7, links.size)

    val link = links.head;
    assertEquals("Endpoints", link.title);
    assertEquals("/service1/endpoints", link.href);
    assertEquals(true, link.isRelative);
  }

}