package eu.bioemergences.mhammons.latomate.models.timer

import scala.concurrent.duration.FiniteDuration

sealed trait Request

case class Stop(self: Respondee) extends Request
case object Start extends Request
case class ChangeWarningPoint(warningPoint: FiniteDuration) extends Request
case object ReenableWarning extends Request
case class AdjustDuration(adjustment: FiniteDuration) extends Request
private[timer] case object Tick extends Request
case object Shutdown extends Request
