import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Dashboard homepage page" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port)

      browser.pageSource must contain("Dashboard - Applications")
    }
  }

  "Application Service1 page" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port + "/applications/Service1")

      browser.pageSource must contain("Applications / (Service1)")
    }
  }

  "Environments page" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port + "/environments")

      browser.pageSource must contain("Dashboard - Environments")
    }
  }

  "Environment INT page" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port + "/environments/int")

      browser.pageSource must contain("Environments / (int)")
    }
  }

  "Dashboard config page" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port + "/admin/config/current")

      browser.pageSource must contain("Dashboard configuration")
    }
  }

  "Dashboard config history page" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port + "/admin/config/history")

      browser.pageSource must contain("Dashboard configuration history")
    }
  }

  // Commented out due to JS error - Cannot find function bind in object function () {...}. (http://cdnjs.cloudflare.com/ajax/libs/vis/4.4.0/vis.js#15214)
  //  "Deployment history page" should {
  //
  //    "work from within a browser" in new WithBrowser {
  //
  //      browser.goTo("http://localhost:" + port + "/history")
  //
  //      browser.pageSource must contain("Deployment history")
  //    }
  //  }
}
