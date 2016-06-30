name := "conductor"
version := "0.0.1"
scalaVersion := "2.11.7"

scalacOptions := Seq( "-unchecked", "-deprecation", "-feature" )
fork in run := true
mainClass in Compile := Some("uk.org.empty.conductor.Main")

/* Production dependencies */
libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.+",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.+", // akka logging handler that uses slf4j

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.1.+", // slf4j implementation
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.+", // scala slf4j wrapper, replaces slf4s

  // Spray
  "io.spray" %% "spray-http" % "1.3.+", // Models of HTTP objects
  "io.spray" %% "spray-can" % "1.3.+", // Low-level akka-based HTTP server
  "io.spray" %% "spray-routing" % "1.3.+", // Routing with DSL for spray-can
  "io.spray" %% "spray-client" % "1.3.+", // Higher-level HTTP client library
  "io.spray" %% "spray-json" % "1.3.+", // JSON support

  // Consul (there's a "scala-consul" but it pulls in all of play and doesn't work without some play process running )
  "com.github.dcshock" % "consul-rest-client" % "0.+",

  // Influx
  // While in here, install telegraph in the VMs. Can celiometer write to
  // influx?

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
