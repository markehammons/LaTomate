package eu.bioemergences.mhammons.latomate.models

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import eu.bioemergences.mhammons.latomate.controllers.PomodoroControllerImpl
import eu.bioemergences.mhammons.latomate.models.pomodoro.{
  Configuration,
  Implementation
}

object RootModel {
  def init: Behavior[RootVocabulary] =
    Behaviors
      .receive[RootVocabulary] {
        case (context, SpawnPomodoroModel(controller)) =>
          controller.setModel(
            context.spawn(Implementation.init(controller,
                                              Configuration.default),
                          "PomodoroModel"))
          Behavior.same
      }
      .receiveSignal {
        case (context, PostStop) =>
          context.log.info("shutting down root model")
          Behavior.stopped
      }

  sealed trait RootVocabulary

  final case class SpawnPomodoroModel(controller: PomodoroControllerImpl)
      extends RootVocabulary

  type RootModel = ActorRef[RootVocabulary]
}
