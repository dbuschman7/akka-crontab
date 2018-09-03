version in ThisBuild:= "0.3.2"

val AkkaVersion = "2.5.16"

val Core = Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1", /*    */ // BSD 3-clause
  "com.typesafe.akka" %% "akka-actor" /*              */ % AkkaVersion, /* */ // ApacheV2
  "org.slf4j" /*         */ % "slf4j-api" /*          */ % "1.7.25", /*   */ // MIT
  //
  "org.scalatest" /*    */ %% "scalatest" /*          */ % "3.0.3" % Test, // ApacheV2
  "com.typesafe.akka" %% "akka-testkit" /*            */ % AkkaVersion % Test, // ApacheV2
  "org.slf4j" /*         */ % "slf4j-simple" /*       */ % "1.7.25" % Test // MIT
)

val Streams = Core ++ Seq(
  "com.typesafe.akka" %% "akka-stream" /*             */ % AkkaVersion, // ApacheV2
  //
  "com.typesafe.akka" %% "akka-stream-testkit" /*     */ % AkkaVersion % Test // ApacheV2
)


lazy val common = Seq(
  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  //
  organization := "me.lightspeed7",
  //
  // Compile time optimizations
  publishArtifact in(Test, packageBin) := true, // Publish test jars
  publishArtifact in(Compile, packageDoc) := false, // Disable ScalDoc generation
  publishArtifact in packageDoc := false,
  sources in(Compile, doc) := Seq.empty,
  fork in Test := true,
  //
  scalacOptions ++= Seq(
    "-deprecation", //
    "-encoding", "UTF-8", //
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-unchecked",
    //  "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
    //  "-Ywarn-unused-import"
  ),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oOF"),  //
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
    libraryDependencies ++= Core
  )
  
lazy val streams = project
  .settings(common)
  .settings(
    name := "akka-crontab-streams",
    libraryDependencies ++= Streams
  )
  .dependsOn( //
   core % "test->test;compile->compile" //
  )

lazy val crontab = (project in file("."))
   .settings(common)
   .aggregate(core, streams)

