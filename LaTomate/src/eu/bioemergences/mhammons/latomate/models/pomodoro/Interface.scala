package eu.bioemergences.mhammons.latomate.models.pomodoro

import akka.actor.typed.Behavior
import eu.bioemergences.mhammons.latomate.controllers.PomodoroController

private[pomodoro] trait Interface {

  def init(controller: PomodoroController,
           configuration: Configuration): Behavior[Request]

  def stopped(implicit state: State): Behavior[Request]

  def pomodoro(implicit state: State): Behavior[Request]

  def rest(implicit state: State): Behavior[Request]
}
