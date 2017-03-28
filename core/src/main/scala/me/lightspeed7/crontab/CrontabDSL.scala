package me.lightspeed7.crontab

object CrontabDSL {

  //
  // Predefined
  // //////////////////////////
  def yearly: Cron = Cron(Fixed(0), Fixed(0), Fixed(1), Fixed(1), Every)
  def monthly: Cron = Cron(Fixed(0), Fixed(0), Fixed(1), Every, Every)
  def weekly: Cron = Cron(Fixed(0), Fixed(0), Every, Every, Fixed(0))
  def daily: Cron = Cron(Fixed(0), Fixed(0), Every, Every, Every)
  def hourly: Cron = Cron(Fixed(0), Every, Every, Every, Every)

  def everyDayAt(hour: Int) = Cron(Fixed(0), Fixed(hour), Every, Every, Every)

}