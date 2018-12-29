package eu.bioemergences.mhammons.latomate.models.timer

import akka.actor.ActorPath
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop}
import eu.bioemergences.mhammons.latomate.models.timer.Timer.{Tick, _}

import scala.concurrent.duration.{Duration, FiniteDuration}

case object TimerImpl extends Timer {
  override def init(
      timerDuration: FiniteDuration,
      warningPoint: FiniteDuration,
      tickResolution: FiniteDuration,
      updateReceiver: Option[Timer.Respondee]): Behavior[Timer.Request] = Behaviors.setup{ ctx =>
    Behaviors.withTimers[Timer.Request] { scheduler =>
      val state = new TimerState(timerDuration,
                                 warningPoint,
                                 tickResolution,
                                 updateReceiver,
                                 scheduler)

      receive(state) { (_, message) =>
        message match {
          case Start =>
            scheduler.startPeriodicTimer(TickKey(ctx.self.path), Tick, tickResolution)
            if (state.warningPoint != Duration.Zero)
              preWarn(state)
            else
              postWarn(state)
          case m => genericMessageHandler(state)(m)
        }
      }
    }}

  override def preWarn(timerState: TimerState): Behavior[Timer.Request] =
    receiveMessage(timerState) {
      case Tick =>
        genericTickHandling(timerState)
        if (timerState.remainingTime <= timerState.warningPoint) {
          timerState.updateReceiver.foreach(
            _ ! Warning(timerState.remainingTime))
          postWarn(timerState)
        } else {
          Behaviors.same
        }
      case m => genericMessageHandler(timerState)(m)
    }

  override def postWarn(timerState: TimerState): Behavior[Timer.Request] = {
    receive(timerState) {
      case (ctx, message) =>
        message match {
          case Tick =>
            genericTickHandling(timerState)
            if (timerState.remainingTime == Duration.Zero) {
              timerState.updateReceiver.foreach(_ ! Complete(Duration.Zero))
              Behaviors.stopped
            } else {
              Behaviors.same
            }
          case ReenableWarning =>
            preWarn(timerState)
          case m => genericMessageHandler(timerState)(m)
        }
    }
  }

  private def genericTickHandling(state: TimerState) = {
    state.remainingTime =
      Duration.Zero.max(state.remainingTime - state.tickResolution)
    state.updateReceiver.foreach(
      _ ! Update(state.remainingTime,
                 state.remainingTime / state.timerDuration))
  }

  private def genericMessageHandler(
      state: TimerState): Timer.Request => Behavior[Timer.Request] = {
    case Start => Behaviors.unhandled

    case Stop(respondee) =>
      if (state.updateReceiver.exists(_ != respondee)) {
        state.updateReceiver.foreach(_ ! Complete(state.remainingTime))
      }
      respondee ! Complete(state.remainingTime)
      Behaviors.stopped

    case Tick =>
      Behaviors.unhandled

    case ChangeWarningPoint(newPoint) =>
      if (newPoint >= Duration.Zero) {
        state.warningPoint = newPoint
      }

      Behaviors.same

    case ReenableWarning =>
      Behaviors.unhandled

    case AdjustDuration(adjustment) =>
      state.timerDuration += adjustment
      state.remainingTime += adjustment
      Behaviors.same
  }

  private def postStopHandling(state: TimerState)(
      behavior: Behaviors.Receive[Timer.Request]) = {
    behavior.receiveSignal {
      case (ctx, PostStop) =>
        ctx.log.info("Stopping")
        state.updateReceiver.foreach(_ ! Complete(state.remainingTime))
        state.scheduler.cancel(TickKey(ctx.self.path))
        while(state.scheduler.isTimerActive(TickKey(ctx.self.path))) {
          Thread.sleep(5)
        }
        Behaviors.stopped
    }
  }

  private def receive(state: TimerState)(
      function: (ActorContext[Timer.Request],
                 Timer.Request) => Behavior[Timer.Request]) =
    postStopHandling(state)(Behaviors.receive(function))

  private def receiveMessage(state: TimerState)(
      function: Timer.Request => Behavior[Timer.Request]) =
    postStopHandling(state)(Behaviors.receiveMessage(function))

  private[timer] case class TickKey(path: ActorPath)
}
