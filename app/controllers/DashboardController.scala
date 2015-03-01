package controllers

import model.DashboardModel.{Environment, _}
import model._
import play.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.ws.WS
import play.api.mvc._
import utils.{RequestUtils, ProcessUtils, RegexUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DashboardController extends Controller {

  def application(applicationName: String) = Action.async {
    buildDashboardModel(Option(applicationName)).map { applicationStatus =>
      Ok(views.html.application(applicationStatus))
    }
  }

  def environment(environmentName: String) = Action.async {
    buildDashboardModel(Option.empty, Option(environmentName)).map { applicationStatus =>
      Ok(views.html.environment(applicationStatus))
    }
  }

  def applications = Action.async {
    buildDashboardModel().map { statusList =>
      Ok(views.html.applications(statusList))
    }
  }

  def environments = Action.async {
    buildDashboardModel().map { statusList =>
      Ok(views.html.environments(statusList))
    }
  }

  // Utility methods

  def buildDashboardModel(applicationName: Option[String] = Option.empty, environmentName: Option[String] = Option.empty): Future[List[ApplicationStatus]] = {
    val applicationFilter: (Application) => Boolean = { application => if (applicationName.isDefined) application.name == applicationName.get else true }
    val applicationList: List[Application] = DashboardModel.create().applications.filter(applicationFilter).toList
    val applicationStatusFutureList: List[Future[ApplicationStatus]] = applicationList.flatMap { application =>
      for {
        environment <- application.environments
        if environmentName.isEmpty || environment.name == environmentName.get
      } yield {
        RequestUtils.buildApplicationStatusPage(application, environment)
      }
    }

    val statusFutureList: Future[List[ApplicationStatus]] = Future.sequence(applicationStatusFutureList).map(_.toList)
    statusFutureList
  }




}