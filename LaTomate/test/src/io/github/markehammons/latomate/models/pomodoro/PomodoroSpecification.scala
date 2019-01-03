package io.github.markehammons.latomate.models.pomodoro

import akka.actor.testkit.typed.Effect.{MessageAdapter, Spawned}
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.github.markehammons.latomate.controllers.PomodoroController
import io.github.markehammons.latomate.models.pomodoro
import io.github.markehammons.latomate.models.timer
import org.scalamock.scalatest.MockFactory
import org.scalatest.{DiagrammedAssertions, WordSpec}

import scala.concurrent.duration._

class PomodoroSpecification
    extends WordSpec
    with DiagrammedAssertions
    with MockFactory {

  val nullActor: Behavior[timer.Request] = Behaviors.receiveMessage {
    case timer.Shutdown => Behaviors.stopped
    case _ => Behaviors.same
  }

  "A pomodoro model" should {
    "handle the start phase properly in all aspects" in {
      val mockController = stub[PomodoroController]

      val mockTimer = stub[timer.Interface]

      val workDuration = 25.minutes

      (mockTimer.init _).when(where { case _ => true }).returns(nullActor)

      val timerModel = BehaviorTestKit(
        pomodoro.defaultModel.init(mockController, Configuration.default.copy(timerImplementation = mockTimer)))

      val ma = timerModel.expectEffectType[MessageAdapter[timer.Response, TimerResponse]]

      timerModel.run(Start)
      val childMailbox = timerModel.childTestKit(timerModel.expectEffectType[Spawned[timer.Request]].ref).selfInbox()

      childMailbox.expectMessage(timer.Start)

      (0 to 24).map(_.minute).reverse.foreach(t => timerModel.run(ma.adapt(timer.Update(t, t/workDuration))))

      timerModel.run(Snooze)

      childMailbox.expectMessage(timer.AdjustDuration(5.minutes))

      timerModel.run(Stop)

      childMailbox.expectMessage(timer.Shutdown)

      val stateMailbox = TestInbox[State]()

      timerModel.run(GetState(stateMailbox.ref))

      val state = stateMailbox.receiveMessage()

      //childMailbox.expectMessage(Timer.Shutdown)

      (mockTimer.init _).verify(state.workDuration, state.warningPoint, state.tickPeriod, Some(state.timerInterface))
      (mockController.updateTimer _)
        .verify(where { case (_, _) => true })
        .repeat(25)
      (mockController.stopTimer _).verify("Stopped").twice()
      (mockController.startWork _).verify("Pomodoro", workDuration)
      (mockController.disableSnooze _).verify()
    }

    "cycle through its states appropriately" in {
      val mockController = stub[PomodoroController]

      val mockTimer = stub[timer.Interface]

      (mockTimer.init _).when(where { case _ => true }).returns(nullActor)

      val timerModel = BehaviorTestKit(
        pomodoro.defaultModel.init(mockController, Configuration.default.copy(timerImplementation = mockTimer))
      )

      val stateMailbox = TestInbox[State]()

      timerModel.run(GetState(stateMailbox.ref))

      val state = stateMailbox.receiveMessage()

      val ma = timerModel.expectEffectType[MessageAdapter[timer.Response, TimerResponse]]

      for(_ <- 0 until Configuration.default.pomodoros) {
        timerModel.run(Start)

        val pTMailbox = timerModel.childTestKit(timerModel.expectEffectType[Spawned[timer.Request]].ref).selfInbox()
        pTMailbox.expectMessage(timer.Start)
        timerModel.run(ma.adapt(timer.Complete(Duration.Zero)))

        pTMailbox.expectMessage(timer.Shutdown)

        val rTMailbox = timerModel.childTestKit(timerModel.expectEffectType[Spawned[timer.Request]].ref).selfInbox()
        rTMailbox.expectMessage(timer.Start)
        timerModel.run(ma.adapt(timer.Complete(Duration.Zero)))

        rTMailbox.expectMessage(timer.Shutdown)
      }

      (mockTimer.init _).verify(state.workDuration, state.warningPoint, state.tickPeriod, Some(state.timerInterface)).repeat(4)
      (mockTimer.init _).verify(state.shortRestDuration, Duration.Zero, state.tickPeriod, Some(state.timerInterface)).repeat(3)
      (mockTimer.init _).verify(state.longRestDuration, state.warningPoint, state.tickPeriod, Some(state.timerInterface))

      (mockController.stopTimer _).verify("Stopped").repeat(5)
      (mockController.startWork _).verify("Pomodoro", state.workDuration).repeat(4)
      (mockController.startBreak _).verify("Break Time!", state.shortRestDuration).repeat(3)
      (mockController.startBreak _).verify("Break Time!", state.longRestDuration)
    }

//    "enter rest mode after the allotted period has passed after the start command has been sent" in {
//      val mockController = mock[PomodoroController]
//
//      val workDuration = 25.minutes
//
//      val shortRestDuration = 5.minutes
//
//      (mockController.updateTimer _)
//        .expects(where { case (_, _) => true })
//        .repeat(24)
//      (mockController.stopTimer _).expects("Stopped")
//      (mockController.startWork _).expects("Pomodoro", workDuration)
//      (mockController.startBreak _).expects("Break Time!", shortRestDuration)
//      (mockController.periodEndingNotification _).expects()
//      (mockController.periodCompleteNotification _).expects()
//
//      val timerModel = BehaviorTestKit(
//        pomodoro.defaultModel.init(dummyTimerImpl, 4, mockController))
//
//      timerModel.run(Start)
//    }

//    "enter the stopped cycle after completing a short rest period" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState =
//        ModelState(false, 0, mockController, tickPeriod = 1.minute)
//
//      val ticks =
//        (timerModelState.shortRestDuration / timerModelState.tickPeriod).ceil
//          .toInt
//
//      (mockController.updateTimer _)
//        .expects(where { case (_, _) => true })
//        .repeat(ticks - 1)
//
//      (mockController.periodCompleteNotification _)
//        .expects()
//
//      (mockController.startBreak _).expects("Break Time!",
//                                            timerModelState.shortRestDuration)
//
//      (mockController.stopTimer _).expects("Stopped")
//
//      val timerModel = BehaviorTestKit(Impl.restPeriod(timerModelState))
//
//      for (_ <- 0 until ticks) {
//        timerModel.run(Tick)
//      }
//    }
//
//    "enter a long rest period after 4 pomodoros" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState =
//        ModelState(false, 4, mockController, tickPeriod = 1.minute)
//
//      val ticks =
//        (timerModelState.longRestDuration / timerModelState.tickPeriod).ceil
//          .toInt
//
//      (mockController.updateTimer _)
//        .expects(where { case (_, _) => true })
//        .repeat(ticks - 1)
//
//      (mockController.periodEndingNotification _).expects()
//
//      (mockController.periodCompleteNotification _).expects()
//
//      (mockController.startBreak _).expects("Break Time!",
//                                            timerModelState.longRestDuration)
//
//      (mockController.stopTimer _).expects("Stopped")
//
//      val timerModel = BehaviorTestKit(Impl.restPeriod(timerModelState))
//
//      for (_ <- 0 until ticks) {
//        timerModel.run(Tick)
//      }
//    }
//
//    "complete a full work/rest mode cycle successfully" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState =
//        ModelState(false, 0, mockController, tickPeriod = 1.minutes)
//
//      val workTicks =
//        (timerModelState.workDuration / timerModelState.tickPeriod).ceil.toInt
//
//      val restTicks =
//        (timerModelState.shortRestDuration / timerModelState.tickPeriod).ceil
//          .toInt
//
//      (mockController.updateTimer _)
//        .expects(where { case (_, _) => true })
//        .repeat(28)
//      (mockController.stopTimer _).expects("Stopped").twice()
//      (mockController.startWork _).expects("Pomodoro",
//                                           timerModelState.workDuration)
//      (mockController.startBreak _).expects("Break Time!",
//                                            timerModelState.shortRestDuration)
//      (mockController.periodEndingNotification _).expects()
//      (mockController.periodCompleteNotification _).expects().twice()
//
//      val timerModel = BehaviorTestKit(Impl.stopped(timerModelState))
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 0)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)
//
//      timerModel.run(Start)
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)
//
//      for (_ <- 0 until workTicks) {
//        timerModel.run(Tick)
//      }
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 1)
//
//      for (_ <- 0 until restTicks) {
//        timerModel.run(Tick)
//      }
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 2)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 1)
//    }
//
//    "complete four full work/rest mode cycles correctly" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState =
//        ModelState(false, 0, mockController, tickPeriod = 1.minute)
//
//      val workTicks =
//        (timerModelState.workDuration / timerModelState.tickPeriod).ceil.toInt
//
//      val restTicks =
//        (timerModelState.shortRestDuration / timerModelState.tickPeriod).ceil.toInt
//
//      val longRestTicks =
//        (timerModelState.longRestDuration / timerModelState.tickPeriod).ceil
//          .toInt
//
//      (mockController.updateTimer _)
//        .expects(where { case (_, _) => true })
//        .repeat((workTicks - 1) * 4 + (restTicks - 1) * 3 + longRestTicks - 1)
//
//      (mockController.stopTimer _).expects("Stopped").repeat(5)
//      (mockController.startWork _)
//        .expects("Pomodoro", timerModelState.workDuration)
//        .repeat(4)
//      (mockController.startBreak _)
//        .expects("Break Time!", timerModelState.shortRestDuration)
//        .repeat(3)
//
//      (mockController.startBreak _)
//        .expects("Break Time!", timerModelState.longRestDuration)
//
//      (mockController.periodEndingNotification _).expects().repeat(5)
//      (mockController.periodCompleteNotification _).expects().repeat(8)
//
//      val timerModel = BehaviorTestKit(Impl.stopped(timerModelState))
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 0)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)
//
//      for (run <- 0 until timerModelState.pomodorosTillLongRest) {
//        timerModel.run(Start)
//
//        assert(
//          timerModel
//            .logEntries()
//            .count(_.message.contains("Stopped")) == run + 1)
//        assert(
//          timerModel
//            .logEntries()
//            .count(_.message.contains("pomodoro")) == run + 1)
//        assert(timerModel.logEntries().count(_.message.contains("rest")) == run)
//
//        for (_ <- 0 until workTicks) {
//          timerModel.run(Tick)
//        }
//
//        assert(
//          timerModel
//            .logEntries()
//            .count(_.message.contains("Stopped")) == run + 1)
//        assert(
//          timerModel
//            .logEntries()
//            .count(_.message.contains("pomodoro")) == run + 1)
//        assert(
//          timerModel.logEntries().count(_.message.contains("rest")) == run + 1)
//
//        for (_ <- 0 until (if (run == 3) longRestTicks else restTicks)) {
//          timerModel.run(Tick)
//        }
//
//        assert(
//          timerModel
//            .logEntries()
//            .count(_.message.contains("Stopped")) == run + 2)
//        assert(
//          timerModel
//            .logEntries()
//            .count(_.message.contains("pomodoro")) == run + 1)
//        assert(
//          timerModel.logEntries().count(_.message.contains("rest")) == run + 1)
//
//      }
//    }
//
//    "not stop when stopped" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState = ModelState(false, 0, mockController)
//
//      (mockController.stopTimer _).expects("Stopped").noMoreThanOnce()
//
//      val timerModel = BehaviorTestKit(Impl.stopped(timerModelState))
//
//      timerModel.run(Stop)
//
//      assert(timerModel.isAlive)
//    }
//
//    "snooze only a certain number of times" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState =
//        ModelState(false, 0, mockController, snoozeLimit = 3)
//
//      val workTicks =
//        (timerModelState.workDuration / timerModelState.tickPeriod).ceil.toInt
//
//      val snoozeTicks =
//        (timerModelState.snoozeLength / timerModelState.tickPeriod).ceil.toInt
//
//      (mockController.startWork _).expects("Pomodoro",
//                                           timerModelState.workDuration)
//
//      (mockController.updateTimer _)
//        .expects(where { case (_, _) => true })
//        .repeat((workTicks + snoozeTicks * timerModelState.snoozeLimit) - 1)
//
//      (mockController.disableSnooze _).expects().repeat(2)
//
//      (mockController.periodEndingNotification _).expects()
//
//      (mockController.periodCompleteNotification _).expects()
//
//      (mockController.startBreak _).expects("Break Time!",
//                                            timerModelState.shortRestDuration)
//
//      val timerModel = BehaviorTestKit(Impl.pomodoro(timerModelState))
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 0)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)
//
//      for (_ <- 0 until timerModelState.snoozeLimit + 1) {
//        timerModel.run(Snooze)
//      }
//
//      for (_ <- 0 until workTicks) {
//        timerModel.run(Tick)
//      }
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 0)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 0)
//
//      for (_ <- 0 until snoozeTicks * timerModelState.snoozeLimit) {
//        timerModel.run(Tick)
//      }
//
//      assert(timerModel.logEntries().count(_.message.contains("Stopped")) == 0)
//      assert(timerModel.logEntries().count(_.message.contains("pomodoro")) == 1)
//      assert(timerModel.logEntries().count(_.message.contains("rest")) == 1)
//    }
//
//    "still count pomodoros stopped midway as pomodoros" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState =
//        ModelState(false,
//                        0,
//                        mockController,
//                        pomodorosTillLongRest = 1,
//                        workDuration = 1.minute,
//                        tickPeriod = 1.minute)
//
//      (mockController.startWork _)
//        .expects("Pomodoro", timerModelState.workDuration)
//        .twice()
//
//      (mockController.stopTimer _).expects("Stopped")
//
//      (mockController.startBreak _).expects("Break Time!",
//                                            timerModelState.longRestDuration)
//
//      (mockController.periodCompleteNotification _).expects()
//
//      val timerModel = BehaviorTestKit(Impl.pomodoro(timerModelState))
//
//      timerModel.run(Stop)
//
//      timerModel.run(Start)
//
//      timerModel.run(Tick)
//    }
//
//    "stop from the rest state" in {
//      val mockController = mock[PomodoroController]
//
//      val timerModelState =
//        ModelState(false, 0, mockController)
//
//      (mockController.startBreak _).expects("Break Time!",
//                                            timerModelState.shortRestDuration)
//
//      (mockController.stopTimer _).expects("Stopped")
//
//      val timerModel = BehaviorTestKit(Impl.restPeriod(timerModelState))
//
//      timerModel.run(Stop)
//
//      timerModel.run(Shutdown)
//
//      assert(!timerModel.isAlive)
//    }
  }
}
