package eu.bioemergences.mhammons.latomate.models

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop}

import scala.concurrent.duration._

object TimerModel {
  def stopped(state: TimerModelState): Behavior[TimerVocab] = Behaviors.setup {
    ctx =>
      ctx.log.debug(s"Stopped with state $state")
      state.controller.stopTimer("Stopped")

      Behaviors
        .receiveMessagePartial[TimerVocab] {
          case Start =>
            pomodoro(state)
          case Shutdown =>
            Behaviors.stopped
        }
        .receiveSignal {
          case (_, PostStop) =>
            ctx.log.info("shutting down...")
            Behaviors.stopped
        }
  }

  def pomodoro(state: TimerModelState) = Behaviors.setup[TimerVocab] { ctx =>
    ctx.log.debug(s"Entering a pomodoro with state $state")
    val body = {
      val timer =
        new TimerOld(state.workDuration,
                  Some(state.warningPoint),
                  state.snoozeLength,
                  state.snoozeLimit)

      state.controller.startWork("Pomodoro", state.workDuration)

      Behaviors
        .receiveMessagePartial[TimerVocab] {
          case Tick =>
            timer.tick(state.tickPeriod)
            if (timer.isComplete()) {
              state.controller.periodCompleteNotification()
              restPeriod(state.copy(pomodoros = state.pomodoros + 1))
            } else {
              if (timer.shouldWarn()) {
                state.controller.periodEndingNotification()
              }

              state.controller.updateTimer(
                timer.getRemainingTime().toMillis,
                timer.getRemainingTime() / state.workDuration)
              Behaviors.same
            }
          case Stop =>
            stopped(state.copy(pomodoros = state.pomodoros + 1))

          case Snooze =>
            timer.snooze()
            if (!timer.canSnooze) {
              state.controller.disableSnooze()
            }
            Behaviors.same

          case Shutdown =>
            Behaviors.stopped
        }
        .receiveSignal {
          case (_, PostStop) =>
            ctx.log.info("Shutting down...")
            Behaviors.stopped
        }
    }

    if (state.withScheduling) {
      Behaviors.withTimers[TimerVocab] { scheduler =>
        scheduler.startPeriodicTimer(TickKey, Tick, state.tickPeriod)
        body
      }
    } else {
      body
    }
  }

  def restPeriod(state: TimerModelState) = Behaviors.setup[TimerVocab] { ctx =>
    ctx.log.debug(s"Entering rest state with $state")
    val body = {
      val restDuration = if (state.pomodoros < state.pomodorosTillLongRest) {
        state.shortRestDuration
      } else {
        state.longRestDuration
      }

      val timer = new TimerOld(restDuration,
                            if (restDuration == state.longRestDuration)
                              Some(state.warningPoint)
                            else None,
                            Duration.Zero,
                            0)

      state.controller.startBreak("Break Time!", restDuration)

      val nState = {
        if (restDuration == state.longRestDuration) {
          state.copy(pomodoros = 0)
        } else {
          state
        }
      }

      Behaviors
        .receiveMessagePartial[TimerVocab] {
          case Tick =>
            timer.tick(state.tickPeriod)
            if (timer.isComplete()) {
              state.controller.periodCompleteNotification()
              stopped(nState)
            } else {
              if (timer.shouldWarn()) {
                state.controller.periodEndingNotification()
              }

              state.controller.updateTimer(
                timer.getRemainingTime().toMillis,
                timer.getRemainingTime() / restDuration)
              Behaviors.same
            }

          case Stop =>
            stopped(nState)

          case Shutdown =>
            Behaviors.stopped
        }
        .receiveSignal {
          case (_, PostStop) =>
            ctx.log.info("Shutting down...")
            Behaviors.stopped
        }
    }

    if (state.withScheduling) {
      Behaviors.withTimers[TimerVocab] { scheduler =>
        scheduler.startPeriodicTimer(TickKey, Tick, state.tickPeriod)
        body
      }
    } else {
      body
    }
  }

  type TimerModel = ActorRef[TimerVocab]

  sealed trait TimerVocab

  case object Start extends TimerVocab
  case object Stop extends TimerVocab
  case object Snooze extends TimerVocab
  case object Tick extends TimerVocab
  case object Shutdown extends TimerVocab

  case object TickKey
}
