package eu.bioemergences.mhammons.latomate.models

import scala.concurrent.duration.{Duration, FiniteDuration}

class Timer(private var timerDuration: FiniteDuration,
            private var warningDurationPoint: Option[FiniteDuration],
            snoozeDuration: FiniteDuration,
            private var snoozes: Int) {
  def tick(durationSince: FiniteDuration): FiniteDuration = {
    timerDuration -= durationSince
    timerDuration
  }

  def shouldWarn(): Boolean = {
    warningDurationPoint.exists { wD =>
      if (wD >= timerDuration) {
        warningDurationPoint = None
        true
      } else {
        false
      }
    }
  }

  def getRemainingTime(): FiniteDuration = timerDuration

  def snooze(): FiniteDuration = {
    if (snoozes > 0) {
      timerDuration += snoozeDuration
      snoozes -= 1
    }
    timerDuration
  }

  def isComplete(): Boolean = {
    if (timerDuration > Duration.Zero) {
      false
    } else {
      true
    }
  }

  def canSnooze: Boolean = {
    snoozes > 0
  }
}
