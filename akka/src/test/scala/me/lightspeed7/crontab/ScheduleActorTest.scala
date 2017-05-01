package me.lightspeed7.crontab

import java.time.LocalDateTime

import org.scalatest.FunSuiteLike

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import scala.concurrent.duration._

import akka.pattern.ask
import akka.util.Timeout
import akka.testkit.TestActorRef
import java.time.temporal.ChronoUnit
import scala.concurrent.Await

class ScheduleActorTest
    extends TestKit(ActorSystem("Scheduling"))
    with FunSuiteLike
    with Matchers
    with ImplicitSender
    with BeforeAndAfterAll
    with SchedulingLogic {

  implicit val ec = system.dispatcher
  implicit val timeout = Timeout.apply(5 seconds)

  System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")

  override def afterAll = TestKit.shutdownActorSystem(system)

  // NOT A REAL TEST 
  ignore("Run Testing Actor") {
    val ref = TestActorRef[TestActor]
    val now = LocalDateTime.now()

    Thread.sleep(60 * 1000 * 1000) // will NOT make this block fail
  }

  test("Testing NextIteration Business Logic") {
    val cron = Cron(Every, Every, Every, Every, Every)
    val nextRun = Await.result(nextIteration(cron, 5 seconds), Duration.Inf)
    val now = LocalDateTime.now
    val delta = now.until(nextRun.time, ChronoUnit.MILLIS)
    delta should be > (0L)
    delta should be < (60L * 1000)
  }

  test("Testing CalcTimeDistance Business Logic") {
    val seconds = 30L
    val inTheFuture = LocalDateTime.now.plusSeconds(30)
    val percent75 = ((seconds * 0.75) * 1000).toLong
    val threshold = 1000L

    val waitFor = calcTimeDistance(inTheFuture)
    val delta = waitFor.delta

    delta should be > (percent75 - threshold)
    delta should be < (percent75 + threshold)
  }
}

class TestActor extends Actor {

  val cron = Cron(Every, Every, Every, Every, Every)

  val events = new java.util.ArrayList[LocalDateTime]()

  val scheduler = context.actorOf(Props(classOf[ScheduleActor], CronConfig(self, cron)))

  def receive: Actor.Receive = {
    case time: LocalDateTime =>
      println("recieving event"); events.add(time)
    case unknown => println("Unknown Message - " + unknown)
  }
}