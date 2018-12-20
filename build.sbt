
name := "component_event_trigger"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.11"

enablePlugins(DockerPlugin)

libraryDependencies ++= Seq(
  "com.bdbizviz.dp" % "kafka_2.11" % "1.0.0-SNAPSHOT",
  "com.typesafe.play" % "play-json_2.11" % "2.6.7" ,
  "com.typesafe.akka" %% "akka-actor" % "2.5.6",
  "com.bdbizviz.dp" %  "dp_entities_2.11" % "1.0.17-batch-SNAPSHOT" ,
  "io.kubernetes" % "client-java" % "2.0.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % Test,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2" ,
  "com.typesafe.play" %% "play-ws" % "2.4.3"
)

// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.6"




assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

imageNames in docker := Seq(

  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value)
  )
)



dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8u111-jdk-alpine")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}
imageNames in docker := Seq(

  ImageName(
    registry = Some("192.168.1.107:5000"),
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value)
  )
)