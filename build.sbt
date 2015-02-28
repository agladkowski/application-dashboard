name := "dashboard"

version := "1.0"

lazy val `dashboard` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( cache , ws, json )

libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  