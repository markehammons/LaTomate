package eu.bioemergences.mhammons.latomate.models

import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import eu.bioemergences.mhammons.latomate.controllers.TimerController
import eu.bioemergences.mhammons.latomate.models.TimerModel.{
  Snooze,
  Start,
  Stop,
  Tick,
  Shutdown
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{DiagrammedAssertions, WordSpec}

import scala.concurrent.duration._

class TimerModelSpecification
    extends WordSpec
    with DiagrammedAssertions
    with MockFactory {

  "A TimerModel" should {
    "start ticking when sent the start command" in {
      val mockController = mock[TimerController]

      val timerModelState = TimerModelState(false, 0, mockController)

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
      (mockController.stopTimer _).expects("Stopped")
      (mockController.startWork _).expects("Pomodoro",
                                           timerModelState.workDuration)

      val timerModel = BehaviorTestKit(TimerModel.stopped(timerModelState))

      timerModel.run(Start)

      timerModel.run(Tick)
    }

    "enter rest mode after the alotted period has passed after the start command has been sent" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false, 0, mockController, tickPeriod = 1.minutes)

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
        .repeat(24)
      (mockController.stopTimer _).expects("Stopped")
      (mockController.startWork _).expects("Pomodoro",
                                           timerModelState.workDuration)
      (mockController.startBreak _).expects("Break Time!",
                                            timerModelState.shortRestDuration)
      (mockController.periodEndingNotification _).expects()
      (mockController.periodCompleteNotification _).expects()

      val timerModel = BehaviorTestKit(TimerModel.stopped(timerModelState))

      timerModel.run(Start)

      for (_ <- 0 until 25) {
        timerModel.run(Tick)
      }
    }

    "enter the stopped cycle after completing a short rest period" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false, 0, mockController, tickPeriod = 1.minute)

      val ticks =
        (timerModelState.shortRestDuration / timerModelState.tickPeriod).ceil
          .toInt

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
        .repeat(ticks - 1)

      (mockController.periodCompleteNotification _)
        .expects()

      (mockController.startBreak _).expects("Break Time!",
                                            timerModelState.shortRestDuration)

      (mockController.stopTimer _).expects("Stopped")

      val timerModel = BehaviorTestKit(TimerModel.restPeriod(timerModelState))

      for (_ <- 0 until ticks) {
        timerModel.run(Tick)
      }
    }

    "enter a long rest period after 4 pomodoros" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false, 4, mockController, tickPeriod = 1.minute)

      val ticks =
        (timerModelState.longRestDuration / timerModelState.tickPeriod).ceil
          .toInt

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
        .repeat(ticks - 1)

      (mockController.periodEndingNotification _).expects()

      (mockController.periodCompleteNotification _).expects()

      (mockController.startBreak _).expects("Break Time!",
                                            timerModelState.longRestDuration)

      (mockController.stopTimer _).expects("Stopped")

      val timerModel = BehaviorTestKit(TimerModel.restPeriod(timerModelState))

      for (_ <- 0 until ticks) {
        timerModel.run(Tick)
      }
    }

    "complete a full work/rest mode cycle successfully" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false, 0, mockController, tickPeriod = 1.minutes)

      val workTicks =
        (timerModelState.workDuration / timerModelState.tickPeriod).ceil.toInt

      val restTicks =
        (timerModelState.shortRestDuration / timerModelState.tickPeriod).ceil
          .toInt

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
        .repeat(28)
      (mockController.stopTimer _).expects("Stopped").twice()
      (mockController.startWork _).expects("Pomodoro",
                                           timerModelState.workDuration)
      (mockController.startBreak _).expects("Break Time!",
                                            timerModelState.shortRestDuration)
      (mockController.periodEndingNotification _).expects()
      (mockController.periodCompleteNotification _).expects().twice()

      val timerModel = BehaviorTestKit(TimerModel.stopped(timerModelState))

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 0)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)

      timerModel.run(Start)

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)

      for (_ <- 0 until workTicks) {
        timerModel.run(Tick)
      }

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 1)

      for (_ <- 0 until restTicks) {
        timerModel.run(Tick)
      }

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 2)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 1)
    }

    "complete four full work/rest mode cycles correctly" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false, 0, mockController, tickPeriod = 1.minute)

      val workTicks =
        (timerModelState.workDuration / timerModelState.tickPeriod).ceil.toInt

      val restTicks =
        (timerModelState.shortRestDuration / timerModelState.tickPeriod).ceil.toInt

      val longRestTicks =
        (timerModelState.longRestDuration / timerModelState.tickPeriod).ceil
          .toInt

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
        .repeat((workTicks - 1) * 4 + (restTicks - 1) * 3 + longRestTicks - 1)

      (mockController.stopTimer _).expects("Stopped").repeat(5)
      (mockController.startWork _)
        .expects("Pomodoro", timerModelState.workDuration)
        .repeat(4)
      (mockController.startBreak _)
        .expects("Break Time!", timerModelState.shortRestDuration)
        .repeat(3)

      (mockController.startBreak _)
        .expects("Break Time!", timerModelState.longRestDuration)

      (mockController.periodEndingNotification _).expects().repeat(5)
      (mockController.periodCompleteNotification _).expects().repeat(8)

      val timerModel = BehaviorTestKit(TimerModel.stopped(timerModelState))

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 0)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)

      for (run <- 0 until timerModelState.pomodorosTillLongRest) {
        timerModel.run(Start)

        assert(
          timerModel
            .logEntries()
            .count(_.message.contains("Stopped")) == run + 1)
        assert(
          timerModel
            .logEntries()
            .count(_.message.contains("pomodoro")) == run + 1)
        assert(timerModel.logEntries().count(_.message.contains("rest")) == run)

        for (_ <- 0 until workTicks) {
          timerModel.run(Tick)
        }

        assert(
          timerModel
            .logEntries()
            .count(_.message.contains("Stopped")) == run + 1)
        assert(
          timerModel
            .logEntries()
            .count(_.message.contains("pomodoro")) == run + 1)
        assert(
          timerModel.logEntries().count(_.message.contains("rest")) == run + 1)

        for (_ <- 0 until (if (run == 3) longRestTicks else restTicks)) {
          timerModel.run(Tick)
        }

        assert(
          timerModel
            .logEntries()
            .count(_.message.contains("Stopped")) == run + 2)
        assert(
          timerModel
            .logEntries()
            .count(_.message.contains("pomodoro")) == run + 1)
        assert(
          timerModel.logEntries().count(_.message.contains("rest")) == run + 1)

      }
    }

    "not stop when stopped" in {
      val mockController = mock[TimerController]

      val timerModelState = TimerModelState(false, 0, mockController)

      (mockController.stopTimer _).expects("Stopped").noMoreThanOnce()

      val timerModel = BehaviorTestKit(TimerModel.stopped(timerModelState))

      timerModel.run(Stop)

      assert(timerModel.isAlive)
    }

    "snooze only a certain number of times" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false, 0, mockController, snoozeLimit = 3)

      val workTicks =
        (timerModelState.workDuration / timerModelState.tickPeriod).ceil.toInt

      val snoozeTicks =
        (timerModelState.snoozeLength / timerModelState.tickPeriod).ceil.toInt

      (mockController.startWork _).expects("Pomodoro",
                                           timerModelState.workDuration)

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
        .repeat((workTicks + snoozeTicks * timerModelState.snoozeLimit) - 1)

      (mockController.disableSnooze _).expects().repeat(2)

      (mockController.periodEndingNotification _).expects()

      (mockController.periodCompleteNotification _).expects()

      (mockController.startBreak _).expects("Break Time!",
                                            timerModelState.shortRestDuration)

      val timerModel = BehaviorTestKit(TimerModel.pomodoro(timerModelState))

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 0)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)

      for (_ <- 0 until timerModelState.snoozeLimit + 1) {
        timerModel.run(Snooze)
      }

      for (_ <- 0 until workTicks) {
        timerModel.run(Tick)
      }

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 0)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)

      for (_ <- 0 until snoozeTicks * timerModelState.snoozeLimit) {
        timerModel.run(Tick)
      }

      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 0)
      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
      assert(timerModel.logEntries().count(_.message.contains("rest")) == 1)
    }

    "still count pomodoros stopped midway as pomodoros" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false,
                        0,
                        mockController,
                        pomodorosTillLongRest = 1,
                        workDuration = 1.minute,
                        tickPeriod = 1.minute)

      (mockController.startWork _)
        .expects("Pomodoro", timerModelState.workDuration)
        .twice()

      (mockController.stopTimer _).expects("Stopped")

      (mockController.startBreak _).expects("Break Time!",
                                            timerModelState.longRestDuration)

      (mockController.periodCompleteNotification _).expects()

      val timerModel = BehaviorTestKit(TimerModel.pomodoro(timerModelState))

      timerModel.run(Stop)

      timerModel.run(Start)

      timerModel.run(Tick)
    }

    "stop from the rest state" in {
      val mockController = mock[TimerController]

      val timerModelState =
        TimerModelState(false, 0, mockController)

      (mockController.startBreak _).expects("Break Time!",
                                            timerModelState.shortRestDuration)

      (mockController.stopTimer _).expects("Stopped")

      val timerModel = BehaviorTestKit(TimerModel.restPeriod(timerModelState))

      timerModel.run(Stop)

      timerModel.run(Shutdown)

      assert(!timerModel.isAlive)
    }
  }

}
