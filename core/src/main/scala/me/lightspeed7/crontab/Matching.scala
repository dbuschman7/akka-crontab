package me.lightspeed7.crontab

import java.time.LocalDateTime

trait Matching[T <: Timing] {
  def matches(time: LocalDateTime): Boolean
}

object Matching {
  implicit object StepsMatching extends Matching[Steps] {
    def matches(time: LocalDateTime): Boolean = true
  }
}