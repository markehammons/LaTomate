package eu.bioemergences.mhammons.latomate

//This dummy launcher is necessary cause openjfx 11 wants javafx on the modulepath if your main class extends Application
object Launcher {
  def main(args: Array[String]): Unit = {
    LaTomate.run(args)
  }
}
