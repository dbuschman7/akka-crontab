package me.lightspeed7.crontab

import java.time.LocalDateTime

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try

class StreamSourceTest
  extends TestKit(ActorSystem("Scheduling"))
    with FunSuiteLike
    with BeforeAndAfterAll
    with Matchers {

  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  override def beforeAll: Unit = system.scheduler.schedule(0 seconds, 10 seconds) {
    println(s"Dt = ${LocalDateTime.now}")
  }


  override def afterAll: Unit = mat.shutdown()

  test("Source testing - 2 MINUTE LONG TEST") {
    var firedCount = 0

    val sink = Sink.foreach[LocalDateTime] { dt =>
      firedCount += 1
      println(s"Fired - $dt")
    }

    val src: Source[LocalDateTime, NotUsed] = StreamSource.create(Crontab.everyMinute)

    Try(Await.ready( src.runWith(sink), 120 seconds))

    firedCount shouldBe 2
  }
}
