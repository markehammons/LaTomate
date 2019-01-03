package eu.bioemergences.mhammons.latomate.models.timer

import akka.actor.ActorPath
import akka.actor.typed.scaladsl.TimerScheduler

import scala.concurrent.duration.FiniteDuration

class State(var timerDuration: FiniteDuration,
            var warningPoint: FiniteDuration,
            val tickResolution: FiniteDuration,
            val updateReceiver: Option[Respondee],
            val scheduler: TimerScheduler[Request],
            val path: ActorPath) {

  var remainingTime: FiniteDuration = timerDuration
}
