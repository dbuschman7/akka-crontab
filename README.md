# akka-crontab
[![Bintray](https://img.shields.io/bintray/v/lightspeed7/maven/akka-crontab.svg?maxAge=2592000)](https://bintray.com/lightspeed7/maven/akka-crontab)

Running crontabs with an Akka ActorSystem without a ton of dependencies

See (Wikipedia)[https://en.wikipedia.org/wiki/Cron]

## Summary 
* Uses Java 8 Time API for data and time calculations
* Slf4j for common logging support 
* Minimalized dependencies - KISS 

## Constructing Cron object

Cron object can be constructed from various helper methods and Raw construction

```scala
import me.lightspeed7.crontab.Crontab._

val daily = Crontab.daily
val hourly = Crontab.hourly 
val everyDayAt = Crontab.everyDayAt(12)

val everyHourOnTheHour = Cron(Fixed(0), Every, Every, Every, Every) 
```

## Parsing using StringInterpolator
```scala
import me.lightspeed7.crontab._

val parsed1: Try[Cron] = cron"1 * * * *"
val parsed2: Try[Cron] = Crontab.apply("1 * * * *")

```

## Scheduling - finding the next time from the cron to run

```scala
import me.lightspeed7.crontab._
import scala.concurrent.duration._
cron"".map { implicit cron =>
  val nextRunTime: Future[LocalDateTime] = Schedule.nextScheduledTime(LocalDateTime.now, 5 seconds)
  // ...
}
```

## Running a Cron with Akka Actor 
```scala
import me.lightspeed7.crontab._

class CronActor(cron: Cron) extends Actor {

  val scheduler = context.actorOf(Props(classOf[ScheduleActor], CronConfig(self, cron)))

  def receive: Actor.Receive = {
    case time: LocalDateTime =>
      // cron needs to run
    ...  
  }
}

```


## Running a Cron as an Akka Stream Source 

```scala
import me.lightspeed7.crontab._

val cron: Cron = ... 

val sink = Sink.foreach[LocalDateTime] { dt => println(s"Fired - $dt") }

val src: Source[LocalDateTime, NotUsed] = StreamSource.create(cron)
    
src.runWith(sink) // run your stream

```