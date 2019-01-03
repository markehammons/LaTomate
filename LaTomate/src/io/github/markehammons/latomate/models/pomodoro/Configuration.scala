package io.github.markehammons.latomate.models.pomodoro

import io.github.markehammons.latomate.models.timer

import scala.concurrent.duration._

case class Configuration(pomodoros: Int,
                         timerImplementation: timer.Interface,
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
    timerImplementation = timer.Implementation,
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