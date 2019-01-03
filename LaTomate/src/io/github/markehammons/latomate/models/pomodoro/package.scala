package io.github.markehammons.latomate.models

import akka.actor.typed.ActorRef

package object pomodoro {
  //maybe rename to Ref?
  type Requestee = ActorRef[Request]

  val defaultModel = Implementation
}
