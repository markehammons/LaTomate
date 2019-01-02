package eu.bioemergences.mhammons.latomate.models.pomodoro

import eu.bioemergences.mhammons.latomate.models.timer.{Timer, TimerImpl}

import scala.concurrent.duration._

case class Configuration(pomodoros: Int,
                         timer: Timer,
                         snoozeLimit: Int,
                         snoozeLength: FiniteDuration,
                         tickPeriod: FiniteDuration,
                         workDuration: FiniteDuration,
                         shortRestDuration: FiniteDuration,
                         longRestDuration: FiniteDuration,
                         warningPoint: FiniteDuration,
                         pomodorosTillLongRest: Int)

object Configuration {
  def default = Configuration(
    pomodoros = 4,
    timer = TimerImpl,
    snoozeLimit = 1,
    snoozeLength = 5.minutes,
    tickPeriod = 200.millis,
    workDuration = 25.minutes,
    shortRestDuration = 5.minutes,
    longRestDuration = 20.minutes,
    warningPoint = 3.minutes,
    pomodorosTillLongRest = 4
  )
}