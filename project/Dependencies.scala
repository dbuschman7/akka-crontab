import sbt._, Keys._

object Dependencies {

  val AkkaVersion = "2.5.0"

  val Core = Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5", /*    */ // BSD 3-clause
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

}
