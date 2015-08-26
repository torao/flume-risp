organization := "org.koiroha"

name := "flume-risp"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation", "-encoding", "UTF-8")

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.apache.flume"  % "flume-ng-sdk"  % "1.6.+",
  "org.apache.flume"  % "flume-ng-core" % "1.6.+",
  "org.glassfish.grizzly" % "grizzly-websockets-server" % "2.3.+",
  "org.glassfish.grizzly" % "grizzly-http-client" % "1.+",
  "org.glassfish.grizzly" % "connection-pool" % "2.3.+",
  "com.google.protobuf" % "protobuf-java" % "2.6.+",
  "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
  "org.specs2" %% "specs2-core" % "3.3.+" % Test exclude("org.scala-lang", "scala-library")
)