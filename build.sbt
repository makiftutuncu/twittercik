name := "twittercik"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.27",
  "org.specs2" %% "specs2" % "2.3.7" % "test"
)     

play.Project.playScalaSettings
