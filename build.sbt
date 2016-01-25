import sbtassembly.AssemblyPlugin._

val artifactId        = "maven-badge"
val buildOrganization = "com.github.y2k2mt"
val buildVersion      = "0.1.0"
val buildScalaVersion = "2.11.7"

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
  "org.analogweb" %% "analogweb-scala" % "0.9.11", 
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" 
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
    javaOptions in run += "-Xmx256M",
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
          artifact.name + "-" + module.revision + "." + artifact.extension
    }
  )
).enablePlugins(JavaAppPackaging)
