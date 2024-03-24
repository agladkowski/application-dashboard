

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class DashboardControllerSpec {

  // TODO migrate these test from old play framework to 2.8.x version
//  "Dashboard" should {
//
//    "send 404 on a bad request" in new WithApplication{
//      route(FakeRequest(GET, "/boum")) must beNone
//    }
//
//    "render the applications dashboard page" in new WithApplication{
//      val applicationsPaage = route(FakeRequest(GET, "/applications")).get
//
//      status(applicationsPaage) must equalTo(OK)
//      contentType(applicationsPaage) must beSome.which(_ == "text/html")
//      contentAsString(applicationsPaage) must contain ("Dashboard - Applications")
//    }
//
//    "render the application (Service1) page" in new WithApplication{
//      val service1Page = route(FakeRequest(GET, "/applications/Service1")).get
//
//      status(service1Page) must equalTo(OK)
//      contentType(service1Page) must beSome.which(_ == "text/html")
//      contentAsString(service1Page) must contain ("Applications / (Service1)")
//    }
//
//    "render the environments page" in new WithApplication{
//      val environmentsPage = route(FakeRequest(GET, "/environments")).get
//
//      status(environmentsPage) must equalTo(OK)
//      contentType(environmentsPage) must beSome.which(_ == "text/html")
//      contentAsString(environmentsPage) must contain ("Environments")
//    }
//
//    "render the environment (int) page" in new WithApplication{
//      val intEnvironmentPage = route(FakeRequest(GET, "/environments/int")).get
//
//      status(intEnvironmentPage) must equalTo(OK)
//      contentType(intEnvironmentPage) must beSome.which(_ == "text/html")
//      contentAsString(intEnvironmentPage) must contain ("Environments / (int)")
//    }
//  }
}
