import sbtassembly.AssemblyPlugin._

val artifactId        = "maven-badge"
val buildOrganization = "com.github.y2k2mt"
val buildVersion      = "0.2.0"
val buildScalaVersion = "2.12.4"

val buildSettings = Defaults.defaultSettings ++ Seq (
  organization := buildOrganization,
  version      := buildVersion,
  scalaVersion := buildScalaVersion
)
val asmSettings = assemblySettings ++ Seq (
  assemblyJarName := artifactId + buildVersion + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first 
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

val allDependencies = Seq (
  "org.analogweb" %% "analogweb-scala" % "0.10.1", 
  "org.analogweb" %% "analogweb-circe" % "0.10.1", 
  "org.analogweb" % "analogweb-slf4j" % "0.10.1", 
  "org.analogweb" % "analogweb-netty" % "0.10.1", 
  "io.circe" %% "circe-parser" % "0.8.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "net.databinder.dispatch" %% "dispatch-core" % "0.13.1"
)

val allResolvers = Seq (
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("snapshots")
)

lazy val mavenBadge = Project (
  id = artifactId,
  base = file("."),
  settings = buildSettings ++ asmSettings ++ Seq (
    resolvers ++= allResolvers,
    libraryDependencies ++= allDependencies,
    fork in run := true,
    javaOptions in run ++= Seq("-Xmx64M"),
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
          artifact.name + "-" + module.revision + "." + artifact.extension
    }
  )
).enablePlugins(JavaAppPackaging)
