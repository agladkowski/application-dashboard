ThisBuild / scalaVersion := "2.13.11"

ThisBuild / version := "1.1-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """application-dashboard""",
    libraryDependencies ++= Seq(
      guice,
      caffeine,
      ws,
      "org.webjars" %% "webjars-play" % "2.8.8",
      "org.webjars" % "bootstrap" % "3.3.0",
      "org.webjars" % "font-awesome" % "4.3.0",
      "org.webjars" % "jquery" % "1.11.1",
      "com.google.code.gson" % "gson" % "2.3.1",
      "com.google.inject" % "guice" % "5.1.0",
      "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    )
  )