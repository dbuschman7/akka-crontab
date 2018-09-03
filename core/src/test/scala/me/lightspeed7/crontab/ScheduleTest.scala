package me.lightspeed7.crontab

import java.time.LocalDateTime

import org.scalatest.FunSuite
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class ScheduleTest extends FunSuite {

  def await(in: Future[LocalDateTime]): LocalDateTime = Await.result(in, 5 seconds)

  val dt: LocalDateTime = LocalDateTime.of(2017, 3, 14, 0, 2, 1, 0)

  test("Single Point Schedule Tests") {

    await(Schedule.nextScheduledTime(dt)(cron"1 * * * *".get)) should be(LocalDateTime.of(2017, 3, 14, 1, 1, 0, 0)) // minute
    await(Schedule.nextScheduledTime(dt)(cron"* 1 * * *".get)) should be(LocalDateTime.of(2017, 3, 14, 1, 0, 0, 0)) // hour
    await(Schedule.nextScheduledTime(dt)(cron"* * 1 * *".get)) should be(LocalDateTime.of(2017, 4, 1, 0, 0, 0, 0)) // day of month
    await(Schedule.nextScheduledTime(dt)(cron"* * * 1 *".get)) should be(LocalDateTime.of(2018, 1, 1, 0, 0, 0, 0)) // month
    await(Schedule.nextScheduledTime(dt)(cron"* * * * 1".get)) should be(LocalDateTime.of(2017, 3, 20, 0, 0, 0, 0)) // day of week

    await(Schedule.nextScheduledTime(dt)(cron"*/5 * * * *".get)) should be(LocalDateTime.of(2017, 3, 14, 0, 5, 0, 0)) // minute

    await(Schedule.nextScheduledTime(dt)(cron"1-5 * * * *".get)) should be(LocalDateTime.of(2017, 3, 14, 0, 3, 0, 0)) // minute
    await(Schedule.nextScheduledTime(dt)(cron"1-2 * * * *".get)) should be(LocalDateTime.of(2017, 3, 14, 1, 1, 0, 0)) // minute
  }

  test("Cron that will never run but times out") {
    implicit val ex: ExecutionContextExecutor = ExecutionContext.global
    val result = Await.ready(Schedule.nextScheduledTime(dt, 5 seconds)(cron"0 0 5 31 2".get), 20 seconds)
    result.isCompleted should be(true)
    result.value.get.isFailure should be(true)
    result.recover {
      case e: Throwable =>
        e.getClass.getSimpleName should be("TimeoutException")
    }
  }

}