package eu.bioemergences.mhammons.latomate.models

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import eu.bioemergences.mhammons.latomate.controllers.TimerController

import scala.concurrent.duration._

object TimerModel {
  def init(controller: TimerController): Behavior[TimerVocab] =
    Behaviors.setup { ctx =>
      val snoozeLimit = 1
      val snoozeAmount = 5.minutes
      val tickFrequency = 200.millis
      val workDuration = 25.minutes
      val shortRestDuration = 5.minutes
      val longRestDuration = 20.minutes
      val warningPoint = 3.minutes
      val pomodorosTillLongRest = 4
      var pomodoros = 0

      lazy val stopped: Behavior[TimerVocab] = Behaviors.setup { _ =>
        controller.stopTimer("Stopped")

        Behaviors
          .receiveMessagePartial[TimerVocab] {
            case Start =>
              pomodoro
            case Shutdown =>
              Behaviors.stopped
          }
          .receiveSignal {
            case (_, PostStop) =>
              ctx.log.info("shutting down...")
              Behaviors.stopped
          }
      }

      lazy val pomodoro: Behavior[TimerVocab] = Behaviors.withTimers { timer =>
        timer.startPeriodicTimer(TickKey, Tick, tickFrequency)

        pomodoros += 1
        var startTime = System.currentTimeMillis()
        var snoozes = 0
        var warned = false
        controller.startWork("Pomodoro")
        controller.updateTimer(workDuration.toMillis, 1.0)

        Behaviors
          .receiveMessagePartial[TimerVocab] {
            case Tick =>
              val pomodoroRemaining =
                workDuration - (System.currentTimeMillis() - startTime).millis
              if (pomodoroRemaining <= 0.millis) {
                controller.periodCompleteNotification("Pomodoro finished!")
                controller.startBreak("Break Time!")
                restPeriod
              } else if (pomodoroRemaining <= warningPoint && !warned) {
                controller.updateTimer(pomodoroRemaining.toMillis,
                                       pomodoroRemaining / workDuration)
                controller.periodEndingNotification(
                  s"Pomodoro ending in $pomodoroRemaining")
                warned = true
                Behaviors.same
              } else {
                controller.updateTimer(pomodoroRemaining.toMillis,
                                       pomodoroRemaining / workDuration)
                Behaviors.same
              }
            case Stop =>
              controller.resetTimer()
              controller.stopTimer("Stopped")
              stopped

            case Snooze =>
              startTime += snoozeAmount.toMillis
              snoozes += 1
              if (snoozes >= snoozeLimit) {
                controller.disableSnooze()
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

      lazy val restPeriod: Behavior[TimerVocab] = Behaviors.withTimers {
        timer =>
          timer.startPeriodicTimer(TickKey, Tick, tickFrequency)

          val startTime = System.currentTimeMillis()
          val restDuration = if (pomodoros <= pomodorosTillLongRest) {
            shortRestDuration
          } else {
            pomodoros = 0
            longRestDuration
          }

          controller.updateTimer(restDuration.toMillis, 1.0)

          Behaviors
            .receiveMessagePartial[TimerVocab] {
              case Tick =>
                val restRemaining =
                  restDuration - (System.currentTimeMillis() - startTime).millis

                if (restRemaining <= 0.millis) {
                  controller.periodCompleteNotification("Break time is over!")
                  controller.stopTimer("Stopped")
                  controller.resetTimer()
                  stopped
                } else if (restRemaining <= warningPoint && restDuration > 5.minutes) {
                  controller.updateTimer(restRemaining.toMillis,
                                         restRemaining / restDuration)
                  controller.periodEndingNotification(
                    "Break time will end soon!")
                  Behaviors.same
                } else {
                  controller.updateTimer(restRemaining.toMillis,
                                         restRemaining / restDuration)
                  Behaviors.same
                }

              case Stop =>
                controller.stopTimer("Stopped")
                controller.resetTimer()
                stopped

              case Shutdown =>
                Behaviors.stopped
            }
            .receiveSignal {
              case (_, PostStop) =>
                ctx.log.info("Shutting down...")
                Behaviors.stopped
            }
      }

      stopped
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
