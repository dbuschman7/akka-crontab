
lazy val common = Seq( 
   version := "0.3.0",
   testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
  )

lazy val core = project
  .settings(common)
  .settings(
    name := "akka-crontab",
    Dependencies.Core 
  )

lazy val streams = project
  .settings(common)
  .settings(
    name := "akka-streams-crontab",
    Dependencies.Streams
  )

lazy val root = (project in file("."))
   .settings(name := "root-crontab")
   .dependsOn(core, streams)

