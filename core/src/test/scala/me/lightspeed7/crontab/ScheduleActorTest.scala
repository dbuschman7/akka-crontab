package me.lightspeed7.crontab

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest._

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._

class ScheduleActorTest
  extends TestKit(ActorSystem("Scheduling"))
    with FunSuiteLike
    with Matchers
    with ImplicitSender
    with BeforeAndAfterAll
    with SchedulingLogic {

  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout.apply(5 seconds)

  System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  // NOT A REAL TEST 
  ignore("Run Testing Actor") {
    val _ = TestActorRef[TestActor]
    Thread.sleep(60 * 1000 * 1000) // will NOT make this block fail
  }

  test("Testing NextIteration Business Logic") {
    val cron = Cron(Every, Every, Every, Every, Every)
    val nextRun = Await.result(nextIteration(LocalDateTime.now, cron), Duration.Inf)
    val now = LocalDateTime.now
    val delta = now.until(nextRun.time, ChronoUnit.MILLIS)
    delta should be > 0L
    delta should be < (61L * 1000) // 1 second threshold
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

  val scheduler: ActorRef = context.actorOf(Props(classOf[ScheduleActor], CronConfig({ dt => self ! dt }, cron)))

  def receive: Actor.Receive = {
    case time: LocalDateTime =>
      println("receiving event")
      events.add(time)
      ()
    case unknown => println("Unknown Message - " + unknown)
  }
}
