package eu.bioemergences.mhammons.latomate.models

import eu.bioemergences.mhammons.latomate.controllers.TimerController

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

case class TimerModelState(withScheduling: Boolean,
                           pomodoros: Int,
                           controller: TimerController,
                           snoozeLimit: Int = 1,
                           snoozeLength: FiniteDuration = 5.minutes,
                           tickPeriod: FiniteDuration = 200.millis,
                           workDuration: FiniteDuration = 25.minutes,
                           shortRestDuration: FiniteDuration = 5.minutes,
                           longRestDuration: FiniteDuration = 20.minutes,
                           warningPoint: FiniteDuration = 3.minutes,
                           pomodorosTillLongRest: Int = 4)
