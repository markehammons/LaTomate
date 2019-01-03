package io.github.markehammons.latomate.models.pomodoro

import akka.actor.typed.ActorRef
import io.github.markehammons.latomate.models.timer

sealed trait Request

case object Start extends Request
case object Stop extends Request
case object Snooze extends Request
case object Shutdown extends Request
private[pomodoro] case class GetState(requester: ActorRef[State])
    extends Request

case class TimerResponse(response: timer.Response) extends Request
