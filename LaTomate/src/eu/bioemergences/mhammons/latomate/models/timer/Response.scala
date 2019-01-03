package eu.bioemergences.mhammons.latomate.models.timer

import scala.concurrent.duration.FiniteDuration

sealed trait Response

case class Update(timeLeft: FiniteDuration, percentTimeRemaining: Double)
    extends Response
case class Complete(timeLeft: FiniteDuration) extends Response
case class Warning(timeLeft: FiniteDuration) extends Response
