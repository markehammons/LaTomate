package io.github.markehammons.latomate.models.timer

import akka.actor.ActorPath
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import io.github.markehammons.latomate.models.StatefulTypedActor

import scala.concurrent.duration._

case object Implementation
    extends Interface
    with StatefulTypedActor[State, Request] {
  override def init(timerDuration: FiniteDuration,
                    warningPoint: FiniteDuration,
                    tickResolution: FiniteDuration,
                    updateReceiver: Option[Respondee]): Behavior[Request] =
    Behaviors.setup { ctx =>
      Behaviors.withTimers[Request] { scheduler =>
        implicit val state = new State(timerDuration,
                                       warningPoint,
                                       tickResolution,
                                       updateReceiver,
                                       scheduler,
                                       ctx.self.path)

        receiveMessagePartial {
          case Start =>
            scheduler.startPeriodicTimer(TickKey(ctx.self.path),
                                         Tick,
                                         tickResolution)
            if (state.warningPoint != Duration.Zero)
              preWarn(state)
            else
              postWarn(state)
        }
      }
    }

  override def stopped(implicit timerState: State): Behavior[Request] =
    Behaviors.setup { ctx =>
      timerState.scheduler.cancelAll()

      ctx.setReceiveTimeout(timerState.tickResolution + 1.minute, Shutdown)
      predefined
    }

  override def preWarn(implicit timerState: State): Behavior[Request] =
    receiveMessagePartial {
      case Tick =>
        genericTickHandling(timerState)
        if (timerState.remainingTime <= timerState.warningPoint) {
          timerState.updateReceiver.foreach(
            _ ! Warning(timerState.remainingTime))
          postWarn(timerState)
        } else {
          Behaviors.same
        }
    }

  override def postWarn(implicit timerState: State): Behavior[Request] = {
    receiveMessagePartial {
      case Tick =>
        genericTickHandling(timerState)
        if (timerState.remainingTime == Duration.Zero) {
          timerState.updateReceiver.foreach(_ ! Complete(Duration.Zero))
          stopped(timerState)
        } else {
          Behaviors.same
        }
      case ReenableWarning =>
        preWarn(timerState)
    }
  }

  private def genericTickHandling(state: State) = {
    state.remainingTime =
      Duration.Zero.max(state.remainingTime - state.tickResolution)
    state.updateReceiver.foreach(
      _ ! Update(state.remainingTime,
                 state.remainingTime / state.timerDuration))
  }
  private[timer] case class TickKey(path: ActorPath)

  override protected def genericSignalHandler(implicit state: State)
    : PartialFunction[(ActorContext[Request], Signal), Behavior[Request]] = {
    case (ctx, PostStop) =>
      ctx.log.debug("Stopping")
      state.updateReceiver.foreach(_ ! Complete(state.remainingTime))
      state.scheduler.cancel(TickKey(state.path))
      Behaviors.stopped
  }

  override protected def genericHandler(request: Request)(
      implicit state: State): Behavior[Request] = request match {
    case Start => Behaviors.unhandled

    case Stop(respondee) =>
      if (state.updateReceiver.exists(_ != respondee)) {
        state.updateReceiver.foreach(_ ! Complete(state.remainingTime))
      }
      respondee ! Complete(state.remainingTime)
      stopped(state)

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

    case Shutdown =>
      Behaviors.stopped
  }
}
