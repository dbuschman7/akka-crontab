import sbt._, Keys._

object Dependencies {

  val ScalaVersions = Seq("2.11.8", "2.12.1")

  val AkkaVersion = "2.4.17"
  
  val Core = Seq(
    libraryDependencies ++= Seq(
      "org.scalatest"          %% "scalatest"                % "3.0.1"      % "test", // ApacheV2
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",               // BSD 3-clause                
      "com.typesafe.akka"      %% "akka-actor"               % AkkaVersion            // ApacheV2
    )
  )

  val Streams = Seq( 
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-stream"              % AkkaVersion,
      "com.typesafe.akka"      %% "akka-stream-testkit"      % AkkaVersion   % Test
    )
  )
}
