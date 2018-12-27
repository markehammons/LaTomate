package eu.bioemergences.mhammons.latomate.models

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import eu.bioemergences.mhammons.latomate.controllers.PomodoroControllerImpl

object RootModel {
  def init: Behavior[RootVocabulary] =
    Behaviors
      .receive[RootVocabulary] {
        case (context, SpawnTimerModel(controller)) =>
          val timerModelState = PomodoroModelState(true, 0, controller)
          controller.setModel(
            context.spawn(PomodoroModel.stopped(timerModelState), "PomodoroModel"))
          Behavior.same
      }
      .receiveSignal {
        case (context, PostStop) =>
          context.log.info("shutting down root model")
          Behavior.stopped
      }

  sealed trait RootVocabulary

  final case class SpawnTimerModel(controller: PomodoroControllerImpl)
      extends RootVocabulary

  type RootModel = ActorRef[RootVocabulary]
}
