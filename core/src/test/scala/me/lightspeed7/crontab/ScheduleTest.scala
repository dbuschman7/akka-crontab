package me.lightspeed7.crontab

import java.time.LocalDateTime

import org.scalatest.FunSuite
import org.scalatest.Matchers.{ be, convertToAnyShouldWrapper }

class ScheduleTest extends FunSuite {

  test("Single Point Schedule Tests") {
    val dt = LocalDateTime.of(2017, 3, 14, 0, 2, 1, 0);

    Schedule.nextScheduledTime(dt)(cron"1 * * * *".get) should be(LocalDateTime.of(2017, 3, 14, 1, 1, 0, 0)) // minute
    Schedule.nextScheduledTime(dt)(cron"* 1 * * *".get) should be(LocalDateTime.of(2017, 3, 14, 1, 0, 0, 0)) // hour
    Schedule.nextScheduledTime(dt)(cron"* * 1 * *".get) should be(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0)) // day of month
    Schedule.nextScheduledTime(dt)(cron"* * * 1 *".get) should be(LocalDateTime.of(2018, 1, 1, 0, 0, 0, 0)) // month
    Schedule.nextScheduledTime(dt)(cron"* * * * 1".get) should be(LocalDateTime.of(2017, 3, 20, 0, 0, 0, 0)) // day of week

    Schedule.nextScheduledTime(dt)(cron"*/5 * * * *".get) should be(LocalDateTime.of(2017, 3, 14, 0, 5, 0, 0)) // minute

    Schedule.nextScheduledTime(dt)(cron"1-5 * * * *".get) should be(LocalDateTime.of(2017, 3, 14, 0, 3, 0, 0)) // minute
    Schedule.nextScheduledTime(dt)(cron"1-2 * * * *".get) should be(LocalDateTime.of(2017, 3, 14, 1, 1, 0, 0)) // minute
  }

}