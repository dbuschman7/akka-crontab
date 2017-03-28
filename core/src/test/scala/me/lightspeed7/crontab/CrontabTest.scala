package me.lightspeed7.crontab

import java.time.{ LocalDateTime, ZoneId }

import org.scalatest.FunSuite
import org.scalatest.Matchers.{ be, convertToAnyShouldWrapper, convertToStringShouldWrapper }

class CrontabTest extends FunSuite {
  import Crontab._
  import Schedule._

  test("Test Basic Parsing") {
    val zId = ZoneId.systemDefault().toString

    cron"1 * * * *".get.toString should be(s"Cron(Fixed(1),Every,Every,Every,Every)") // minute ( 0-59 )
    cron"* 1 * * *".get.toString should be(s"Cron(Every,Fixed(1),Every,Every,Every)") // hour (0 - 23 )
    cron"* * 1 * *".get.toString should be(s"Cron(Every,Every,Fixed(1),Every,Every)") // day of month ( 1 -31 )
    cron"* * * 1 *".get.toString should be(s"Cron(Every,Every,Every,Fixed(1),Every)") // month ( 1 -12 )
    cron"* * * * 1".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(1))") // day of week ( 0 - 7, Sun to Sat, 7 also Sun )

    cron"*/5 * * * *".get.toString should be(s"Cron(Steps(List(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)),Every,Every,Every,Every)") // fixed
    cron"1,5 * * * *".get.toString should be(s"Cron(Steps(List(1, 5)),Every,Every,Every,Every)") // Divisors
    cron"1-5 * * * *".get.toString should be(s"Cron(Range(1,5),Every,Every,Every,Every)") // range

    cron"* * * * SUN".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(0))") // day of week
    cron"* * * * MON".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(1))") // day of week
    cron"* * * * TUE".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(2))") // day of week
    cron"* * * * WED".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(3))") // day of week
    cron"* * * * THU".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(4))") // day of week
    cron"* * * * FRI".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(5))") // day of week
    cron"* * * * SAT".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(6))") // day of week

    cron"* * * JAN *".get.toString should be(s"Cron(Every,Every,Every,Fixed(1),Every)") // month by name
    cron"* * * FEB *".get.toString should be(s"Cron(Every,Every,Every,Fixed(2),Every)") // month by name
    cron"* * * MAR *".get.toString should be(s"Cron(Every,Every,Every,Fixed(3),Every)") // month by name
    cron"* * * APR *".get.toString should be(s"Cron(Every,Every,Every,Fixed(4),Every)") // month by name
    cron"* * * MAY *".get.toString should be(s"Cron(Every,Every,Every,Fixed(5),Every)") // month by name
    cron"* * * JUN *".get.toString should be(s"Cron(Every,Every,Every,Fixed(6),Every)") // month by name
    cron"* * * JUL *".get.toString should be(s"Cron(Every,Every,Every,Fixed(7),Every)") // month by name
    cron"* * * AUG *".get.toString should be(s"Cron(Every,Every,Every,Fixed(8),Every)") // month by name
    cron"* * * SEP *".get.toString should be(s"Cron(Every,Every,Every,Fixed(9),Every)") // month by name
    cron"* * * OCT *".get.toString should be(s"Cron(Every,Every,Every,Fixed(10),Every)") // month by name
    cron"* * * NOV *".get.toString should be(s"Cron(Every,Every,Every,Fixed(11),Every)") // month by name
    cron"* * * DEC *".get.toString should be(s"Cron(Every,Every,Every,Fixed(12),Every)") // month by name
  }

  test("Prevent invalid use of month") {
    cron"DEC * * * *".isFailure should be(true)
    cron"* DEC * * *".isFailure should be(true)
    cron"* * DEC * *".isFailure should be(true)
    cron"* * * DEC *".isFailure should be(false)
    cron"* * * * DEC".isFailure should be(true)
  }

  test("Prevent invalid use of DOW") {
    cron"MON * * * *".isFailure should be(true)
    cron"* MON * * *".isFailure should be(true)
    cron"* * MON * *".isFailure should be(true)
    cron"* * * MON *".isFailure should be(true)
    cron"* * * * MON".isFailure should be(false)
  }

  test("Prevent invalid use of Nth DOW") {
    cron"1#1 * * * *".isFailure should be(true)
    cron"* 1#1 * * *".isFailure should be(true)
    cron"* * 1#1 * *".isFailure should be(true)
    cron"* * * 1#1 *".isFailure should be(true)
    cron"* * * * 1#1".isFailure should be(false)
  }

  test("Compare domain objects") {
    val step: Steps = Steps(Seq(1, 2, 3))
    (step == Steps(Seq(1, 2, 3))) should be(true)
    (step == step) should be(true)

  }

  test("Timing Every match") {
    matches(Every, LocalDateTime.of(2017, 3, 14, 0, 2, 1, 0), (time) => time.getMinute) should be(true)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = matches(Every, testDate, (time) => time.getMinute)
        val expected = true
        result == expected
    }

    (2010 to 2020).map(exhaustiveDays(_, compare))
  }

  test("Timing Steps match") {
    matches(Steps(Seq(1, 2, 3, 4)), LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0), extMin) should be(true)
    matches(Steps(Seq(1, 3, 4)), LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0), extMin) should be(false)
    matches(Steps(Seq(5, 6, 7, 8)), LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0), extMin) should be(false)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = matches(Steps(Seq(matchDay)), testDate, extDay)
        val expected = testDay == matchDay
        result == expected
    }

    (2010 to 2020).map(exhaustiveDays(_, compare))
  }

  test("Timing Range Test") {
    matches(Range(1, 4), LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0), extMin) should be(true)
    matches(Range(5, 9), LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0), extMin) should be(false)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = matches(Range(matchDay, matchDay), testDate, extDay)
        val expected = testDay == matchDay
        result == expected
    }

    (2010 to 2020).map(exhaustiveDays(_, compare))
  }

  test("Timing Fixed Test") {
    matches(Fixed(1), LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0), extMin) should be(false)
    matches(Fixed(2), LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0), extMin) should be(true)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = matches(Fixed(matchDay), testDate, extDay)
        val expected = testDay == matchDay
        result == expected
    }

    (2010 to 2020).map(exhaustiveDays(_, compare))
  }

  test("Timing NthDow Test") {
    matches(NthDow(1, 2), LocalDateTime.of(2017, 3, 14, 0, 0, 0, 0), extDow) should be(false)
    matches(NthDow(2, 2), LocalDateTime.of(2017, 3, 14, 0, 0, 0, 0), extDow) should be(true)
    matches(NthDow(2, 2), LocalDateTime.of(2017, 3, 7, 0, 0, 0, 0), extDow) should be(false)

    // do the exhaustive
    (2010 to 2020).map(exhaustiveDow(_, NthDow(1, 2)))
  }

  test("Timing LastDow Test") {
    matches(LastDow(0), LocalDateTime.of(2017, 3, 26, 0, 0, 0, 0), extDow) should be(true)

    matches(LastDow(1), LocalDateTime.of(2017, 3, 26, 0, 0, 0, 0), extDow) should be(false)
    matches(LastDow(5), LocalDateTime.of(2017, 3, 24, 0, 0, 0, 0), extDow) should be(false)
    matches(LastDow(5), LocalDateTime.of(2017, 3, 31, 0, 0, 0, 0), extDow) should be(true)

    // do the exhaustive
    (2010 to 2020).map(exhaustiveDow(_, LastDow(2)))
  }

  //
  // Test Helpers
  // ///////////////////////////////////
  type Compare = (LocalDateTime, Int, Int, Int, LocalDateTime, Int, Int, Int) ⇒ Boolean // true == pass 

  // DOW-based logic testing 
  def exhaustiveDow(year: Int, timing: Timing) = {
    val results: Seq[(Int, Int, Int)] = for {
      month <- (1 to 12)
      day <- (1 to LocalDateTime.of(year, month, 1, 0, 0, 0, 0).toLocalDate().lengthOfMonth())
      date = LocalDateTime.of(year, month, day, 0, 0, 0, 0)
      dow = date.getDayOfWeek.getValue()
      if matches(timing, date, extDow)
    } yield {
      (year, month, day)
    }

    results.size should be(12) // must have twelve matches
    results.groupBy { tp => s"${tp._1}-${tp._2}" }.size should be(12) // one date from every month

  }

  // Day-based logic testing 
  def exhaustiveDays(year: Int, compare: Compare): Unit = (1 to 12).map { mth ⇒ exhaustiveDays(year, mth, compare) }
  def exhaustiveDays(year: Int, month: Int, compare: Compare): Unit = {
    val range = (1 to LocalDateTime.of(year, month, 1, 0, 0, 0, 0).toLocalDate().lengthOfMonth())
    for {
      // matching
      matchDay ← range
      matchDate = LocalDateTime.of(year, month, matchDay, 0, 0, 0, 0)
      matchDow = matchDate.getDayOfWeek.getValue % 7
      matchNth = ((matchDay - 1) / 7) + 1
      // testing
      testDay ← range
      testDate = LocalDateTime.of(year, month, testDay, 0, 0, 0, 0)
      testDow = testDate.getDayOfWeek.getValue % 7
      testNth = ((testDay - 1) / 7) + 1
    } yield {
      compare(matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) match {
        case true  ⇒ // pass 
        case false ⇒ fail(f"Match(${matchDate}) - ${matchDay}%2d ${matchDow}%1d ${matchNth}%1d Test - ${testDay}%2d ${testDow}%1d ${testNth}%1d")
      }
    }
  }

}