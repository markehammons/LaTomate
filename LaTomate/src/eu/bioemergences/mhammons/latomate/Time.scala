package eu.bioemergences.mhammons.latomate

object Time {
  def prettyPrintMillis(millis: Long) = {
    f"${millis / 60 / 1000}%02dm ${millis / 1000 % 60}%02ds"
  }
}
