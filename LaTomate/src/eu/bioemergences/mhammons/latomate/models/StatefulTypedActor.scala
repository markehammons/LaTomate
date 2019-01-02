package eu.bioemergences.mhammons.latomate.models

import akka.actor.typed.{Behavior, Signal}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

trait StatefulTypedActor[State, Request] {
  protected def receivePartial(
    partialFunctionConst: PartialFunction[
      (ActorContext[Request], Request),
      Behavior[Request]])(implicit state: State): Behavior[Request] =
    Behaviors.receivePartial[Request] {
      case (ctx, message) =>
        try {
          partialFunctionConst(ctx -> message)
        } catch {
          case _: MatchError =>
            genericHandler(message)
          case e: Throwable => throw e
        }
    }.receiveSignal(genericSignalHandler)

  protected def receive(function: State => (
    ActorContext[Request],
      Request) => Behavior[Request])(implicit state: State) =
    Behaviors.receive(function(state))


  protected def receiveMessage(function:  Request => Behavior[Request])(implicit state: State) = {
    Behaviors.receiveMessage(m => function(m)).receiveSignal(genericSignalHandler(state))
  }

  protected def receiveMessagePartial(function: PartialFunction[Request,Behavior[Request]])(implicit state: State) = {
    Behaviors.receiveMessage[Request]{ request =>
      try {
        function(request)
      } catch {
        case _: MatchError =>
          genericHandler(request)
        case e: Throwable => throw e
      }
    }.receiveSignal(genericSignalHandler)
  }


  protected def genericSignalHandler(implicit state: State): PartialFunction[(ActorContext[Request], Signal), Behavior[Request]]



  protected def genericHandler(request: Request)(implicit state: State): Behavior[Request]
}
