package me.lightspeed7.crontab

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeoutException

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

object Schedule {

  final def initializeTime: LocalDateTime = roundToMinute(roundToSecond(LocalDateTime.now))

  final def nextScheduledTime(time: LocalDateTime, timeout: Duration = 5 seconds)(implicit cron: Cron): Future[LocalDateTime] = {
    val start = LocalDateTime.now
    val endBy: Instant = start.plus(timeout.toMillis, ChronoUnit.MILLIS).toInstant(ZoneOffset.UTC)

    @annotation.tailrec
    def tryNext(time: LocalDateTime): Future[LocalDateTime] = {
      if (endBy isBefore LocalDateTime.now.toInstant(ZoneOffset.UTC))
        Future failed new TimeoutException
      else {
        time match {
          case t if !minMatch(t) ⇒ tryNext(time.plusMinutes(1))
          case t if !hourMatch(t) ⇒ tryNext(time.plusHours(1L).withMinute(nextMinute(time)))
          case t if !dayMatch(t) ⇒ tryNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
          case t if !weekMatch(t) ⇒ tryNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
          case t if !monthMatch(t) ⇒ tryNext(time.plusMonths(1).withDayOfMonth(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
          case _ ⇒ Future successful time
        }
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
    case _ ⇒ time.withSecond(0).plusMinutes(1)
  }

  private final def foldToNext(init: Int, time: LocalDateTime, setter: (Int, LocalDateTime) ⇒ LocalDateTime, timing: Timing, extract: LocalDateTime ⇒ Int): Int = {
    @annotation.tailrec
    def test(idx: Int): Int = {
      if (matches(timing, setter(idx, time), extract)) idx
      else test(idx + 1)
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
      case Every ⇒ true
      case Steps(list) ⇒ list.contains(extract(time))
      case Range(from, to) ⇒ extract(time) >= from && extract(time) <= to
      case Fixed(num) ⇒ extract(time) == num
      case NthDow(dow, nth) ⇒
        if (time.getDayOfWeek.getValue == dow) {
          val fDow = time.withDayOfMonth(1).getDayOfWeek.getValue % 7
          val calculated = (7 - fDow) + ((nth - 1) * 7) + dow + 1
          calculated == time.toLocalDate.getDayOfMonth
        } else false
      case LastDow(dow) ⇒
        if (time.getDayOfWeek.getValue % 7 == dow)  { // force 7 back down to 0
          (time.toLocalDate.lengthOfMonth() - time.getDayOfMonth) < 7
        } else false
    }
  }

  private[crontab] final def extDow(in: LocalDateTime): Int = in.getDayOfWeek.getValue

  private[crontab] final def extMonth(in: LocalDateTime): Int = in.getMonth.getValue

  private[crontab] final def extDay(in: LocalDateTime): Int = in.getDayOfMonth

  private[crontab] final def extHour(in: LocalDateTime): Int = in.getHour

  private[crontab] final def extMin(in: LocalDateTime): Int = in.getMinute

}