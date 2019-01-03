package io.github.markehammons.latomate.models.timer

import akka.actor.typed.Behavior

import scala.concurrent.duration.FiniteDuration

trait Interface {
  def init(timerDuration: FiniteDuration,
           warningPoint: FiniteDuration,
           tickResolution: FiniteDuration,
           updatesReceiver: Option[Respondee]): Behavior[Request]

  def stopped(implicit timerState: State): Behavior[Request]

  def preWarn(implicit timerState: State): Behavior[Request]

  def postWarn(implicit timerState: State): Behavior[Request]
}