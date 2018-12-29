package eu.bioemergences.mhammons.latomate.models.timer

import akka.actor.typed.scaladsl.TimerScheduler

import scala.concurrent.duration.FiniteDuration

class TimerState(var timerDuration: FiniteDuration,
                 var warningPoint: FiniteDuration,
                 val tickResolution: FiniteDuration,
                 val updateReceiver: Option[Timer.Respondee],
                 val scheduler: TimerScheduler[Timer.Request]) {

  var remainingTime: FiniteDuration = timerDuration
}
