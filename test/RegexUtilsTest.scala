import org.junit.Assert._
import org.junit._
import utils.RegexUtils

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class RegexUtilsTest {
  val versionPattern = """\"version\"\:\"([0-9\.\-_A-Z]*)\""""

  @Test
  def testVersionMatched(): Unit = {
    val content = "{\"status\":\"OK\",\"appName\":\"test app\",\"version\":\"1.0.0\"}"
    val version = RegexUtils.findMatch(versionPattern, content)

    println(version)
    assertEquals("1.0.0", version)
  }

  @Test
  def testNoVersionFound(): Unit = {
    val content = "{\"status\":\"OK\",\"appName\":\"test app\"}"
    val version = RegexUtils.findMatch(versionPattern, content)

    println(version)
    assertEquals("", version)
  }

  @Test
  def testMultipleVersionsFound(): Unit = {
    val content = "{\"status\":\"OK\",\"version\":\"2.0.0-SNAPSHOT\",\"appName\":\"test app\",\"microservice.version\":\"1.0.0-SNAPSHOT\"}"
    val version = RegexUtils.findMatch(versionPattern, content)

    println(version)
    assertEquals("2.0.0-SNAPSHOT", version)
  }
}
