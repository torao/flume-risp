organization := "org.koiroha"

name := "flume-risp"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.apache.flume"  % "flume-ng-sdk"  % "1.6.+",
  "org.apache.flume"  % "flume-ng-core" % "1.6.+",
  "org.glassfish.grizzly" % "grizzly-websockets-server" % "2.3.+",
  "com.google.protobuf" % "protobuf-java" % "2.6.+"
)