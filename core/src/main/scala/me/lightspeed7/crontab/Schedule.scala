package me.lightspeed7.crontab

import java.time.LocalDateTime
import java.time.DayOfWeek
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import java.time.temporal.TemporalUnit
import java.time.temporal.ChronoUnit
import java.time.ZoneOffset
import java.time.Instant
import java.util.concurrent.TimeoutException

object Schedule {

  final def initializeTime = roundToMinute(roundToSecond(LocalDateTime.now))

  final def nextScheduledTime(time: LocalDateTime, timeout: Duration = 5 seconds)(implicit cron: Cron): Future[LocalDateTime] = {
    val start = LocalDateTime.now
    val endBy: Instant = start.plus(timeout.toMillis, ChronoUnit.MILLIS).toInstant(ZoneOffset.UTC)

    @annotation.tailrec
    def tryNext(time: LocalDateTime): Future[LocalDateTime] = (endBy isBefore LocalDateTime.now.toInstant(ZoneOffset.UTC)) match {
      case true => Future failed new TimeoutException
      case false => time match {
        case t if !minMatch(t)      ⇒ tryNext(time.plusMinutes(1))
        case t if !hourMatch(time)  ⇒ tryNext(time.plusHours(1L).withMinute(nextMinute(time)))
        case t if !dayMatch(time)   ⇒ tryNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
        case t if !weekMatch(time)  ⇒ tryNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
        case t if !monthMatch(time) ⇒ tryNext(time.plusMonths(1).withDayOfMonth(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
        case _                      ⇒ Future successful time
      }
    }

    tryNext(roundToMinute(roundToSecond(time))) // start it off 
  }

  private final def roundToSecond(time: LocalDateTime): LocalDateTime = time.getNano match {
    case 0 ⇒ time
    case _ ⇒ time.withNano(0).plusSeconds(1)
  }

  private final def roundToMinute(time: LocalDateTime): LocalDateTime = time.getSecond match {
    case 0 ⇒ time
    case n ⇒ time.withSecond(0).plusMinutes(1)
  }

  private final def foldToNext(init: Int, time: LocalDateTime, setter: (Int, LocalDateTime) ⇒ LocalDateTime, timing: Timing, extract: LocalDateTime ⇒ Int): Int = {
    @annotation.tailrec
    def test(idx: Int): Int = matches(timing, setter(idx, time), extract) match {
      case true  ⇒ idx
      case false ⇒ test(idx + 1)
    }
    test(init)
  }

  private final def nextMinute(time: LocalDateTime)(implicit cron: Cron) = foldToNext(0, time, (min, time) ⇒ time.withMinute(min), cron.min, extMin)
  private final def nextHour(time: LocalDateTime)(implicit cron: Cron) = foldToNext(0, time, (hour, time) ⇒ time.withHour(hour), cron.hour, extHour)

  private final def minMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.min, time, extMin)
  private final def hourMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.hour, time, extHour)
  private final def dayMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.day, time, extDay)
  private final def monthMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.month, time, extMonth)
  private final def weekMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.dayOfWeek, time, extDow)

  private[crontab] final def matches(timing: Timing, time: LocalDateTime, extract: LocalDateTime ⇒ Int): Boolean = {
    timing match {
      case Every           ⇒ true
      case Steps(list)     ⇒ list.contains(extract(time))
      case Range(from, to) ⇒ extract(time) >= from && extract(time) <= to
      case Fixed(num)      ⇒ extract(time) == num
      case NthDow(dow, nth) ⇒ {
        time.getDayOfWeek.getValue == dow match {
          case false ⇒ false
          case true ⇒ {
            val fDow = time.withDayOfMonth(1).getDayOfWeek.getValue % 7
            val calculated = (7 - fDow) + ((nth - 1) * 7) + dow + 1
            calculated == time.toLocalDate().getDayOfMonth
          }
        }
      }
      case LastDow(dow) ⇒ {
        (time.getDayOfWeek.getValue % 7 == dow) match { // force 7 back down to 0
          case false ⇒ false
          case true  ⇒ (time.toLocalDate().lengthOfMonth() - time.getDayOfMonth) < 7
        }
      }
    }
  }

  private[crontab] final def extDow(in: LocalDateTime): Int = in.getDayOfWeek.getValue
  private[crontab] final def extMonth(in: LocalDateTime): Int = in.getMonth.getValue
  private[crontab] final def extDay(in: LocalDateTime): Int = in.getDayOfMonth
  private[crontab] final def extHour(in: LocalDateTime): Int = in.getHour
  private[crontab] final def extMin(in: LocalDateTime): Int = in.getMinute

}