import sbt._, Keys._

object Dependencies {

  val AkkaVersion = "2.5.0"

  val Core = Seq(
    "org.scalatest" /*    */ %% "scalatest" /*          */ % "3.0.3" % "test", // ApacheV2
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5", /*    */ // BSD 3-clause       
    "org.slf4j" /*         */ % "slf4j-api" /*          */ % "1.7.25", /*   */ // MIT
    "org.slf4j" /*         */ % "slf4j-simple" /*       */ % "1.7.25" % "test" // MIT
    )

  val Akka = Core ++ Seq(
    "com.typesafe.akka" %% "akka-actor" /*   */ % AkkaVersion, /* */ // ApacheV2
    "com.typesafe.akka" %% "akka-testkit" /* */ % AkkaVersion % Test // ApacheV2
    )

  val Streams = Akka ++ Seq(
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion, /*         */ // ApacheV2
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test // ApacheV2
    )

}
