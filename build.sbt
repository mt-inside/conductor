name := "conductor"
version := "0.0.1"
scalaVersion := "2.11.7"

scalacOptions := Seq( "-unchecked", "-deprecation", "-feature" )
fork in run := true
mainClass in Compile := Some("uk.org.empty.conductor.Main")

/* Production dependencies */
libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.3.+",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.+", // akka logging handler that uses slf4j

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.1.+", // slf4j implementation
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.+", // scala slf4j wrapper, replaces slf4s

  // Spray
  "io.spray" %% "spray-can" % "1.3.+",
  "io.spray" %% "spray-http" % "1.3.+",
  "io.spray" %% "spray-routing" % "1.3.+",
  "io.spray" %% "spray-json" % "1.3.+",

  // Consul (scala-consul pulls in all of play :/ )
  "com.codacy" %% "scala-consul" % "1.+",

  // Influx

  // Misc
  "org.json4s" %% "json4s-native" % "3.2.+"
)

/* Test dependancies */
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.3.+" % "test",
  "org.scalatest" %% "scalatest" % "2.2.+" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.+" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.+" % "test"
)
