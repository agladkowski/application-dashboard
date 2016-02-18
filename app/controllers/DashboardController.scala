package controllers

import model.DashboardModel._
import model._
import play.api.mvc._
import service.{DowntimeHistoryService, VersionHistoryService}
import utils.RequestUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DashboardController extends Controller {

  def application(applicationName: String) = Action.async {
    buildDashboardModel(Option(applicationName)).map { applicationStatus =>
      Ok(views.html.application(applicationStatus))
    }
  }

  def applicationDivAjax(applicationName: String) = Action.async {
    buildDashboardModel(Option(applicationName)).map { applicationStatus =>
      Ok(views.html.common.applicationStatusList(applicationStatus))
    }
  }

  def environment(environmentName: String) = Action.async {
    buildDashboardModel(None, Option(environmentName)).map { applicationStatus =>
      Ok(views.html.environment(applicationStatus))
    }
  }

  def applications = Action.async { implicit request =>
    buildDashboardModel().map { statusList =>
      Ok(views.html.applications(statusList))
    }
  }

  def environments = Action.async {
    buildDashboardModel().map { statusList =>
      Ok(views.html.environments(statusList))
    }
  }

  def history = historyFilter(7)

  def historyFilter(numberOfDays: Int = 7) = Action {
    Ok(views.html.history(VersionHistoryService.versionHistory(numberOfDays), numberOfDays))
  }

  def downtimeHistory = downtimeHistoryFilter(1)

  def downtimeHistoryFilter(numberOfDays: Int = 1) = Action {
    Ok(views.html.downtimeHistory(DowntimeHistoryService.downtimeHistory(numberOfDays), numberOfDays))
  }

  def buildDashboardModel(applicationName: Option[String] = None, environmentName: Option[String] = None): Future[List[ApplicationStatus]] = {
    val applicationList: List[Application] = getApplications(applicationName)
    val applicationStatusFutureList: List[Future[ApplicationStatus]] =
      for {
        application <- applicationList
        environment <- application.environments
        if environmentName.isEmpty || environment.name == environmentName.get
      } yield {
        RequestUtils.buildApplicationStatusPage(application, environment)
      }

    Future.sequence(applicationStatusFutureList)
  }

  def getApplications(applicationName: Option[String] = None): List[Application] = {
    val applicationList: List[Application] = DashboardModel.create().applications.toList
    applicationName match {
      case Some(name) => applicationList.filter( application => application.name == name)
      case None => applicationList
    }
  }

}