package eu.bioemergences.mhammons.latomate.models.pomodoro

import java.util.UUID

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import eu.bioemergences.mhammons.latomate.controllers.PomodoroController
import eu.bioemergences.mhammons.latomate.models.StatefulTypedActor
import eu.bioemergences.mhammons.latomate.models.timer.Timer
import eu.bioemergences.mhammons.latomate.models.timer.Timer.{Complete, Update, Warning}

import scala.concurrent.duration._

case object Impl extends Model with StatefulTypedActor[State, Request] {
  def init(controller: PomodoroController,
           configuration: Configuration): Behavior[Request] = Behaviors.setup {
    ctx =>
      val timerInterface = ctx.messageAdapter(TimerResponse)
      implicit val state = new State(timerInterface, controller, configuration)
      stopped
  }

  override def stopped(implicit state: State): Behavior[Request] = {
    state.controller.stopTimer("Stopped")

    receiveMessagePartial {
      case Start =>
        pomodoro
    }
  }

  override def pomodoro(implicit state: State): Behavior[Request] =
    Behaviors.setup { ctx =>
      ctx.log.info("starting pomodoro")
      state.controller.startWork("Pomodoro", state.workDuration)
      state.pomodoros += 1
      val timer = ctx.spawn(state.timer.init(state.workDuration,
                                             state.warningPoint,
                                             state.tickPeriod,
                                             Some(state.timerInterface)),
                            s"pomodoro-timer-${UUID.randomUUID()}")
      var snoozes = 0
      timer ! Timer.Start


      receiveMessagePartial {
        case TimerResponse(timerMessage) =>
          timerMessage match {
            case Update(timeLeft, percentTimeRemaining) =>
              state.controller.updateTimer(timeLeft, percentTimeRemaining)
              Behaviors.same
            case Complete(_) =>
              state.controller.periodCompleteNotification()
              rest
            case Warning(_) =>
              state.controller.periodEndingNotification()
              Behaviors.same
          }
        case Stop =>
          stopped
        case Snooze =>
          if (snoozes < state.snoozeLimit) {
            snoozes += 1
            timer ! Timer.AdjustDuration(state.snoozeLength)
          }
          Behaviors.same
      }
    }

  override def rest(implicit state: State): Behavior[Request] =
    Behaviors.setup { ctx =>
      val restDuration = if (state.pomodoros < state.pomodorosTillLongRest) {
        state.shortRestDuration
      } else {
        state.pomodoros = 0
        state.longRestDuration
      }

      state.controller.startBreak("Break Time!", restDuration)

      val warningPoint =
        if (restDuration == state.shortRestDuration)
          Duration.Zero
        else
          state.warningPoint

      val timer = ctx.spawn(state.timer.init(restDuration,
                                             warningPoint,
                                             state.tickPeriod,
                                             Some(state.timerInterface)),
                            s"rest-timer-${UUID.randomUUID()}")

      timer ! Timer.Start

      receiveMessagePartial {
        case TimerResponse(tM) =>
          tM match {
            case Update(timeLeft, percentTimeRemaining) =>
              state.controller.updateTimer(timeLeft, percentTimeRemaining)
              Behaviors.same
            case Warning(_) =>
              state.controller.periodEndingNotification()
              Behaviors.same
            case Complete(_) =>
              state.controller.periodCompleteNotification()
              stopped
          }
        case Stop =>
          stopped
      }
    }

  override protected def genericSignalHandler(implicit state: State)
    : PartialFunction[(ActorContext[Request], Signal), Behavior[Request]] = {
    case (ctx, PostStop) =>
      ctx.log.debug("Stopping")
      Behaviors.stopped
  }

  override protected def genericHandler(request: Request)(
      implicit state: State): Behavior[Request] = request match {
    case Stop     => stopped(state)
    case Shutdown => Behaviors.stopped
    case GetState(requester) =>
      requester ! state
      Behaviors.same
    case _ => Behaviors.unhandled
  }
}
