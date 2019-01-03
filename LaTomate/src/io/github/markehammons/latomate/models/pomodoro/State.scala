package io.github.markehammons.latomate.models.pomodoro

import io.github.markehammons.latomate.controllers.PomodoroController
import io.github.markehammons.latomate.models.timer

import scala.concurrent.duration._

private[pomodoro] class State(val timerInterface: timer.Respondee,
                              val controller: PomodoroController,
                              configuration: Configuration) {

  var pomodoros: Int = configuration.pomodoros
  var snoozeLimit: Int = configuration.snoozeLimit
  var snoozeLength: FiniteDuration = configuration.snoozeLength
  val tickPeriod: FiniteDuration = configuration.tickPeriod
  var workDuration: FiniteDuration = configuration.workDuration
  var shortRestDuration: FiniteDuration = configuration.shortRestDuration
  var longRestDuration: FiniteDuration = configuration.longRestDuration
  var warningPoint: FiniteDuration = configuration.warningPoint
  var pomodorosTillLongRest: Int = configuration.pomodorosTillLongRest
  val timerImplementation: timer.Interface = configuration.timerImplementation
}
