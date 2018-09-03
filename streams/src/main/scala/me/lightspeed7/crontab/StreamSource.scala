package me.lightspeed7.crontab

import java.time.LocalDateTime

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}

object StreamSource {

  def create(cron: Cron)(implicit system: ActorSystem): Source[LocalDateTime, NotUsed] = {
    Source
      .queue[LocalDateTime](1, OverflowStrategy.dropHead)
      .mapMaterializedValue { queue: SourceQueueWithComplete[LocalDateTime] =>
        system.actorOf(Props(classOf[ScheduleActor], CronConfig({ dt => queue.offer(dt) }, cron)))
        NotUsed
      }
  }

}
