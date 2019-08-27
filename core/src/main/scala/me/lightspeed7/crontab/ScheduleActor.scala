package me.lightspeed7.crontab

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

// Model 
sealed case class CronConfig(receiver: LocalDateTime => Unit, cron: Cron, threshold: Duration = 5 seconds)

sealed case class NextRunTime(time: LocalDateTime)

sealed case class WaitFor(delta: Long) {
  def toDuration: FiniteDuration = FiniteDuration(delta, TimeUnit.MILLISECONDS)
}

case object CalcNextDistance {}

// Actor
class ScheduleActor(implicit config: CronConfig)
  extends Actor
    with SchedulingLogic
    with LogHelper {

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  protected val log: Logger = LoggerFactory.getLogger(classOf[ScheduleActor].getSimpleName)

  private var nextTime: Option[LocalDateTime] = None

  override def preStart(): Unit = {
    generateNextRun(LocalDateTime.now)
    ()
  }

  def receive: Actor.Receive = {
    case next: NextRunTime =>
      nextTime = Option(next.time)
      self ! CalcNextDistance
    case CalcNextDistance => nextTime
      .map { next => self ! calcTimeDistance(next); true }
      .orElse {
        logWarn("CalcNextDistance - No time to use for for calculation")
        None
      }
      ()
    case waitFor: WaitFor =>
      waitFor.delta match {
        case n if n < config.threshold.toMillis ⇒ nextTime.map(fireCron)
        case _ ⇒ scheduleWait(waitFor)
      }
      ()
  }

  // Helpers
  private def fireCron(time: LocalDateTime) = {
    logInfo("fireCron - time - " + time)
    config.receiver(time) // fire
    generateNextRun(time)
  }

  private def scheduleWait(waitFor: WaitFor) = {
    val dur = waitFor.toDuration
    logDebug("scheduleWait - Delta - " + dur)
    system.scheduler.scheduleOnce(dur, self, CalcNextDistance) // another wait cycle
  }

  private def generateNextRun(last: LocalDateTime) = nextIteration(last.plusSeconds(1), config.cron)
    .map { next =>
      logInfo("nextIteration - Next scheduled time - " + next.toString)
      self ! NextRunTime(next.time)
    }.recover {
    case ex =>
      logError("nextIteration - Stopping", ex)
      context.stop(self)
  }
}

trait TimeTracking {
  self: Actor =>

}

trait SchedulingLogic {

  protected def nextIteration(last: LocalDateTime, cron: Cron)(implicit ec: ExecutionContext): Future[NextRunTime] = {
    Schedule.nextScheduledTime(last)(cron)
      .map {
        time: LocalDateTime => NextRunTime(time)
      }
  }

  protected def calcTimeDistance(nextTime: LocalDateTime): WaitFor = {
    val delta: Long = (0.75 * ChronoUnit.MILLIS.between(LocalDateTime.now(), nextTime)).toLong
    WaitFor(delta)
  }

}

trait LogHelper {

  protected def log: Logger

  protected final def logDebug(msg: String, t: Throwable = null)(implicit config: CronConfig): Unit = {
    if (log.isDebugEnabled()) {
      log.debug(config.cron.toString + " - " + msg, t)
    }
  }

  protected final def logInfo(msg: String, t: Throwable = null)(implicit config: CronConfig): Unit = {
    if (log.isInfoEnabled()) {
      log.info(config.cron.toString + " - " + msg, t)
    }
  }

  protected final def logWarn(msg: String, t: Throwable = null)(implicit config: CronConfig): Unit = {
    if (log.isWarnEnabled()) {
      log.warn(config.cron.toString + " - " + msg, t)
    }
  }

  protected final def logError(msg: String, t: Throwable = null)(implicit config: CronConfig): Unit = {
    if (log.isErrorEnabled()) {
      log.error(config.cron.toString + " - " + msg, t)
    }
  }
}