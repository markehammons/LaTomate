package eu.bioemergences.mhammons.latomate.models.timer

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.{ManualTime, ScalaTestWithActorTestKit, TestProbe}
import eu.bioemergences.mhammons.latomate.models.timer.Timer._
import org.scalatest.{DiagrammedAssertions, WordSpecLike}

import scala.concurrent.duration._

class TimerImplSpecification
    extends ScalaTestWithActorTestKit(ManualTime.config)
    with WordSpecLike
    with DiagrammedAssertions {
  val manualTime = ManualTime()

  def createTimer() = {
    val receiver = TestProbe[Timer.Response]
    spawn(TimerImpl.init(25.minutes, 5.minutes, 1.minute, Some(receiver.ref)),
          s"TimerImpl-${UUID.randomUUID()}") -> receiver
  }

  "A TimerImpl" should {
    "only start ticking after start has been received" in {
      val (cTimerImpl, receiver) = createTimer()

      receiver.expectNoMessage()

      cTimerImpl ! Stop(receiver.ref)

      receiver.expectMessage(Complete(25.minutes))

      Thread.sleep(200)
    }

    "send time updates periodically after start has been received" in {
      val (cTimerImpl, receiver) = createTimer()

      cTimerImpl ! Start

      manualTime.timePasses(1.minute)

      receiver.expectMessage(Update(24.minute, 24.minute / 25.minute))

      cTimerImpl ! Stop(receiver.ref)

      receiver.expectMessage(Complete(24.minute))

      Thread.sleep(200)
    }

    "send a completion signal after time's up" in {
      val (cTimerImpl, receiver) = createTimer()

      cTimerImpl ! Start

      manualTime.timePasses(25.minutes)

      val result = receiver.receiveN(27)

      assert(result.count {
        case Update(_, _) => true
        case _            => false
      } == 25 && result.count {
        case Warning(_) => true
        case _          => false
      } == 1 && result.count {
        case Complete(_) => true
        case _           => false
      } == 1)

      Thread.sleep(200)
    }

    "adjust time successfully" in {
      val (cTimerImpl, receiver) = createTimer()

      cTimerImpl ! AdjustDuration(5.minutes)

      cTimerImpl ! Stop(receiver.ref)

      receiver.expectMessage(Complete(30.minutes))

      Thread.sleep(200)
    }

    "change and re-enable warning point" in {
      val (cTimerImpl, receiver) = createTimer()

      cTimerImpl ! Start

      manualTime.timePasses(20.minutes)

      val result = receiver.receiveN(21)

      assert(result.count{ case Warning(_) => true case _ => false} == 1)

      cTimerImpl ! ChangeWarningPoint(3.minutes)

      cTimerImpl ! ReenableWarning

      manualTime.timePasses(2.minutes)

      val result2 = receiver.receiveN(3)
      assert(result2.count{ case Warning(_) => true case _ => false} == 1)

      cTimerImpl ! Stop(receiver.ref)

      receiver.expectMessage(Complete(3.minutes))

      Thread.sleep(200)
    }
  }

}
