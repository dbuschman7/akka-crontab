package me.lightspeed7.crontab

import java.time.LocalDateTime
import java.time.DayOfWeek

object Schedule {

  def initializeTime = roundToMinute(roundToSecond(LocalDateTime.now))

  def nextScheduledTime(time: LocalDateTime)(implicit cron: Cron): LocalDateTime = findNext(roundToMinute(roundToSecond(time)))

  private def roundToSecond(time: LocalDateTime): LocalDateTime = time.getNano match {
    case 0 ⇒ time
    case _ ⇒ time.withNano(0).plusSeconds(1)
  }

  private def roundToMinute(time: LocalDateTime): LocalDateTime = time.getSecond match {
    case 0 ⇒ time
    case n ⇒ time.withSecond(0).plusMinutes(1)
  }

  @annotation.tailrec
  private final def findNext(time: LocalDateTime)(implicit cron: Cron): LocalDateTime = time match {
    case t if !minMatch(t)      ⇒ findNext(time.plusMinutes(1))
    case t if !hourMatch(time)  ⇒ findNext(time.plusHours(1L).withMinute(nextMinute(time)))
    case t if !dayMatch(time)   ⇒ findNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
    case t if !weekMatch(time)  ⇒ findNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
    case t if !monthMatch(time) ⇒ findNext(time.plusMonths(1).withDayOfMonth(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
    case _                      ⇒ time
  }

  private def foldToNext(init: Int, time: LocalDateTime, setter: (Int, LocalDateTime) ⇒ LocalDateTime, timing: Timing, extract: LocalDateTime ⇒ Int): Int = {
    @annotation.tailrec
    def test(idx: Int): Int = matches(timing, setter(idx, time), extract) match {
      case true  ⇒ idx
      case false ⇒ test(idx + 1)
    }
    test(init)
  }

  private def nextMinute(time: LocalDateTime)(implicit cron: Cron) = foldToNext(0, time, (min, time) ⇒ time.withMinute(min), cron.min, extMin)
  private def nextHour(time: LocalDateTime)(implicit cron: Cron) = foldToNext(0, time, (hour, time) ⇒ time.withHour(hour), cron.hour, extHour)

  private def minMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.min, time, extMin)
  private def hourMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.hour, time, extHour)
  private def dayMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.day, time, extDay)
  private def monthMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.month, time, extMonth)
  private def weekMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = matches(cron.dayOfWeek, time, extDow)

  private[crontab] def matches(timing: Timing, time: LocalDateTime, extract: LocalDateTime ⇒ Int): Boolean = {
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

  private[crontab] def extDow(in: LocalDateTime): Int = in.getDayOfWeek.getValue
  private[crontab] def extMonth(in: LocalDateTime): Int = in.getMonth.getValue
  private[crontab] def extDay(in: LocalDateTime): Int = in.getDayOfMonth
  private[crontab] def extHour(in: LocalDateTime): Int = in.getHour
  private[crontab] def extMin(in: LocalDateTime): Int = in.getMinute

}