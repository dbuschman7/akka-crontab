
lazy val common = Seq(
  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  //
  organization := "me.lightspeed7",
  version := "0.3.1",
  //
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  //
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/dbuschman7/akka-crontab/tree/release-" + version.value),
    "scm:git:https://github.com/dbuschman7/akka-crontab.git",
    Some("scm:git:https://github.com/dbuschman7/akka-crontab.git"))),
  pomIncludeRepository := { _ => false },
  pomExtra :=
      <developers>
        <developer>
          <id>dbuschman7</id>
          <name>David Buschman</name>
          <email>david.buschman7@gmail.com</email>
        </developer>
      </developers>
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

