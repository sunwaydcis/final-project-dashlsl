ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "CryptoDash",
    libraryDependencies ++= {
      val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux")     => "linux"
        case n if n.startsWith("Mac")       => "mac"
        case n if n.startsWith("Windows")   => "win"
        case _                              => throw new Exception("Unknown Platform!")
      }
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "21.0.5" classifier osName)
    },
    libraryDependencies ++= Seq("org.scalafx" %% "scalafx" % "21.0.0-R32"),
    libraryDependencies ++= Seq("com.softwaremill.sttp.client3" %% "core" % "3.8.15"),
    libraryDependencies ++= Seq("org.web3j" % "core" % "4.12.2"),
    libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-xml" % "2.1.0"),
    libraryDependencies ++= Seq("com.lihaoyi" %% "ujson" % "2.0.0"),


      Compile / resourceDirectory := baseDirectory.value / "src" / "main" / "resources",
    Compile / resourceGenerators += Def.task {
      val resources = (Compile / resourceDirectory).value
      val targetDir = (Compile / classDirectory).value
      val log = streams.value.log

      val copiedFiles = (resources ** "*.fxml").get.map { file =>
        val targetFile = targetDir / file.relativeTo(resources).get.getPath
        IO.copyFile(file, targetFile)
        log.info(s"Copied resource: ${file.getName} to $targetFile")
        targetFile
      }
      copiedFiles
    }
  )
