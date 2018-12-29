package eu.bioemergences.mhammons.latomate.models.timer

import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.FiniteDuration

trait Timer {
  def init(timerDuration: FiniteDuration,
           warningPoint: FiniteDuration,
           tickResolution: FiniteDuration,
           updatesReceiver: Option[Timer.Respondee]): Behavior[Timer.Request]

  def preWarn(timerState: TimerState): Behavior[Timer.Request]

  def postWarn(timerState: TimerState): Behavior[Timer.Request]
}

object Timer {
  sealed trait Request

  case class Stop(self: Respondee) extends Request
  case object Start extends Request
  case class ChangeWarningPoint(warningPoint: FiniteDuration)
    extends Request
  case object ReenableWarning extends Request
  case class AdjustDuration(adjustment: FiniteDuration) extends Request
  private[timer] case object Tick extends Request

  sealed trait Response

  case class Update(timeLeft: FiniteDuration, percentTimeRemaining: Double)
    extends Response
  case class Complete(timeLeft: FiniteDuration) extends Response
  case class Warning(timeLeft: FiniteDuration) extends Response

  type Respondee = ActorRef[Response]
  type Requestee = ActorRef[Request]
}
