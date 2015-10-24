name := "dashboard"

version := "1.0"

lazy val `dashboard` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq( cache , ws, json )

libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.3.0",
  "org.webjars" % "font-awesome" % "4.3.0",
  "org.webjars" % "jquery" % "1.11.1"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
