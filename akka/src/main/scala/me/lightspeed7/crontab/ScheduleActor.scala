package me.lightspeed7.crontab

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{ Duration, DurationInt, FiniteDuration }

import akka.actor.Actor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Await

// Model 
sealed case class CronConfig(receiver: ActorRef, cron: Cron, threshold: Duration = 5 seconds)

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

  import context._

  protected val log = LoggerFactory.getLogger(classOf[ScheduleActor].getSimpleName)

  private var nextTime: Option[LocalDateTime] = None

  override def preStart(): Unit = generateNextRun

  def receive: Actor.Receive = {
    case next: NextRunTime => {
      nextTime = Option(next.time)
      self ! CalcNextDistance
    }
    case CalcNextDistance => nextTime
      .map { next => self ! calcTimeDistance(next) }
      .orElse { logWarn("CalcNextDistance - No time to use for for calculation"); None }
    case waitFor: WaitFor => waitFor.delta match {
      case n if n < config.threshold.toMillis ⇒ nextTime.map(fireCron)
      case n                                  ⇒ scheduleWait(waitFor)
    }
  }

  // Helpers
  private def fireCron(time: LocalDateTime) = {
    logInfo("fireCron - time - " + time)
    config.receiver ! time // fire
    generateNextRun
  }

  private def scheduleWait(waitFor: WaitFor) = {
    val dur = waitFor.toDuration
    logDebug("scheduleWait - Delta - " + dur)
    system.scheduler.scheduleOnce(dur, self, CalcNextDistance) // another wait cycle
  }

  private def generateNextRun = nextIteration(config.cron, config.threshold)
    .map { next =>
      logInfo("nextIteration - Next scheduled time - " + next.toString())
      self ! NextRunTime(next.time)
    }.recover {
      case ex =>
        logError("nextIteration - Stopping", ex)
        context.stop(self)
    }
}

trait TimeTracking { self: Actor =>

}

trait SchedulingLogic {

  protected def nextIteration(cron: Cron, threshold: Duration)(implicit ec: ExecutionContext): Future[NextRunTime] = {
    val nextStart = LocalDateTime.now.plusSeconds(threshold.toSeconds)
    Schedule.nextScheduledTime(nextStart)(cron)
      .map {
        case time: LocalDateTime => NextRunTime(time)
      }
  }

  protected def calcTimeDistance(nextTime: LocalDateTime): WaitFor = {
    val delta: Long = (0.75 * ChronoUnit.MILLIS.between(LocalDateTime.now(), nextTime)).toLong
    WaitFor(delta)
  }

}

trait LogHelper {

  protected def log: Logger

  protected final def logDebug(msg: String, t: Throwable = null)(implicit config: CronConfig) = {
    if (log.isDebugEnabled()) {
      log.debug(config.cron.toString + " - " + msg, t)
    }
  }

  protected final def logInfo(msg: String, t: Throwable = null)(implicit config: CronConfig) = {
    if (log.isInfoEnabled()) {
      log.info(config.cron.toString + " - " + msg, t)
    }
  }

  protected final def logWarn(msg: String, t: Throwable = null)(implicit config: CronConfig) = {
    if (log.isWarnEnabled()) {
      log.warn(config.cron.toString + " - " + msg, t)
    }
  }

  protected final def logError(msg: String, t: Throwable = null)(implicit config: CronConfig) = {
    if (log.isErrorEnabled()) {
      log.error(config.cron.toString + " - " + msg, t)
    }
  }
}