package io.github.markehammons.latomate.models

import akka.actor.typed.ActorRef

package object timer {
  type Respondee = ActorRef[Response]
  type Requestee = ActorRef[Request]
}
