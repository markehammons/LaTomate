import java.io.{ByteArrayOutputStream, PrintWriter}
import java.util.spi.ToolProvider

scalaVersion := "2.12.7"

val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val sttpVersion = "1.5.1"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp" %% "core",
  "com.softwaremill.sttp" %% "akka-http-backend"
).map(_ % sttpVersion)

val akkaVersion = "2.5.19"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream",
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-actor-typed"
).map(_ % akkaVersion)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test


scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint"
)

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)

val osName = System.getProperty("os.name") match {
  case name if name.startsWith("Linux") => "linux"
  case name if name.startsWith("Mac") => "mac"
  case name if name.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

val javafxVersion = "11.0.1"

libraryDependencies ++= Seq(
  "javafx-base",
  "javafx-controls",
  "javafx-fxml",
  "javafx-graphics",
  "javafx-media",
).map("org.openjfx" % _ % javafxVersion classifier osName)

fork := true

libraryDependencies += "com.jfoenix" % "jfoenix" % "9.0.8"

mainClass in Compile := Some("eu.bioemergences.mhammons.latomate.Launcher")

enablePlugins(JavaAppPackaging)

def runTool(name: String, arguments: Seq[String]): Either[String,String] = {
  val maybeTool: Option[ToolProvider] = {
    val _tool = ToolProvider.findFirst(name)
    if(_tool.isPresent) {
      Some(_tool.get())
    } else {
      None
    }
  }

  val result = for(tool <- maybeTool) yield {
    val stdOut = new ByteArrayOutputStream()
    val errOut = new ByteArrayOutputStream()
    tool.run(new PrintWriter(stdOut), new PrintWriter(errOut), arguments: _*)
    (new String(stdOut.toByteArray), new String(errOut.toByteArray))
  }

  result
    .toRight(s"Could not find tool $name in your java development environment")
    .flatMap{ case (ret,err) =>
      if(ret.contains("Error:") || err.nonEmpty) {
        Left(ret + err)
      } else {
        Right(ret -> "")
      }
    }
    .map(_._1)
}

val moduleDependencies = taskKey[Array[String]]("outputs the jdk module dependency information of our classpath")

moduleDependencies := {
  val logger = streams.value
  logger.log.info("getting module dependencies from jdeps...")

  val classPathValue = (dependencyClasspath in Runtime)
    .value
    .map(_.data.getAbsolutePath)

  val command = Seq("-recursive", "--list-deps") ++ classPathValue

  logger.log.info(s"jdeps ${command.mkString(" ")}")

  runTool("jdeps", command)
    .map(_
      .split('\n')
      .filter(!_.isEmpty)
      .map(_
        .filter(!_.isWhitespace)
        .split('/')
        .head
      )
      .distinct
      .filter(_ != "JDKremovedinternalAPI")
    )
    .fold(sys.error, mods => {
      logger.log.info("done generating module dependencies...")
      mods
    })
}

val jlink = taskKey[File]("generates a java runtime for the project")

jlink := {
  val logger = streams.value

  logger.log.info("generating runtime with jlink...")

  val outputFile = target.value / s"${name.value}-runtime"

  if(outputFile.exists()) {
    logger.log.info("deleting already generated runtime")
    IO.delete(outputFile)
  }

  val modulesToAdd = Seq("--add-modules", moduleDependencies.value.mkString(","))

  val outputArgument = Seq("--output", outputFile.absolutePath)

  val command = Seq("--no-header-files","--no-man-pages","--compress=2","--strip-debug") ++ modulesToAdd ++ outputArgument

  logger.log.debug(s"command: jlink ${command.mkString(" ")}")

  runTool("jlink", command).map(_ => outputFile).fold(sys.error, identity)
}

scalafmtOnCompile := true

mappings in Universal ++= {
  val dir = jlink.value
  (dir.**(AllPassFilter) --- dir).pair(file => IO.relativize(dir.getParentFile, file))
}

bashScriptExtraDefines ++= Seq(s"JAVA_HOME=$$app_home/../${jlink.value.getName}")
