import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import config.DashboardHistory
import controllers.DashboardController
import model.DashboardModel.ApplicationStatus
import play.api.GlobalSettings
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.{Logger, api}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration



/**
 * Created by andrzej on 23/03/2015.
 */
object Global extends GlobalSettings {

  override def onStart(app: api.Application): Unit = {
    super.onStart(app)
    Logger.info("On application start....")
    val dashboardHistoryUpdateActor = Akka.system.actorOf(Props(new DashboardHistoryUpdateActor()), name = "dashboardHistoryUpdateActor")
    Akka.system.scheduler.schedule(
      Duration.create(0, TimeUnit.MILLISECONDS),
      //Duration.create(30, TimeUnit.SECONDS),
      Duration.create(5, TimeUnit.MINUTES),
      dashboardHistoryUpdateActor,
      "update"
    )
  }

  class DashboardHistoryUpdateActor extends Actor {
    def receive = {
      case msg: String =>
        Logger.info(s"Updating dashboard history")
        val dashboardStatus: Future[List[ApplicationStatus]] = DashboardController.buildDashboardModel()
        dashboardStatus.map {statusList: List[ApplicationStatus] =>
          statusList.map { status: ApplicationStatus =>
            DashboardHistory.recordHistory(status)
          }
        }
        // https://developers.google.com/chart/interactive/docs/gallery/timeline
    }
  }
}