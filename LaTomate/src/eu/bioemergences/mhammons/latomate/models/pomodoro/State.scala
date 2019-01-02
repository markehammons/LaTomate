package eu.bioemergences.mhammons.latomate.models.pomodoro

import eu.bioemergences.mhammons.latomate.controllers.PomodoroController
import eu.bioemergences.mhammons.latomate.models.timer.Timer

import scala.concurrent.duration._

private[pomodoro] class State(val timerInterface: Timer.Respondee,
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
  val timer: Timer = configuration.timer
}
