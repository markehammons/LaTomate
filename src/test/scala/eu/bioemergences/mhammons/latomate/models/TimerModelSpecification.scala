package eu.bioemergences.mhammons.latomate.models

import akka.actor.testkit.typed.scaladsl.ManualTime
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import eu.bioemergences.mhammons.latomate.controllers.TimerController
import eu.bioemergences.mhammons.latomate.models.TimerModel.Start
import org.scalamock.scalatest.MockFactory
import org.scalatest.{DiagrammedAssertions, WordSpecLike}
import scala.concurrent.duration._

class TimerModelSpecification
    extends ScalaTestWithActorTestKit(ManualTime.config)
    with WordSpecLike
    with DiagrammedAssertions
    with MockFactory {
  val manualTime: ManualTime = ManualTime()

  "A TimerModel" should {
    "start ticking when sent the start command" in {
      val mockController = mock[TimerController]

      (mockController.updateTimer _)
        .expects(where { case (_, _) => true })
        .twice()
      (mockController.stopTimer _).expects("Stopped")
      (mockController.startWork _).expects("Pomodoro")

      val timerModel = spawn(TimerModel.init(mockController))

      timerModel ! Start

      manualTime.timePasses(200.millis)
    }
  }

}
