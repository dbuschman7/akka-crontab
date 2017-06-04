
lazy val common = Seq(
   organization := "me.lightspeed7",
   crossScalaVersions := Seq("2.11.11", "2.12.2"), 
   version := "0.3.0",
   testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
  )

lazy val core = project
  .settings(common)
  .settings(
    name := "akka-crontab",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Dependencies.Core
  )
  
lazy val streams = project
  .settings(common)
  .settings(
    name := "akka-crontab-streams",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Dependencies.Streams
  )
  .dependsOn( //
   core % "test->test;compile->compile" //
  )

lazy val root = (project in file("."))
   .settings(common)
   .aggregate(core, streams)

