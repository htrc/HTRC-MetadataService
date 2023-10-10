import com.typesafe.sbt.packager.docker.*

showCurrentGitBranch

inThisBuild(Seq(
  organization := "org.hathitrust.htrc",
  organizationName := "HathiTrust Research Center",
  organizationHomepage := Some(url("https://www.hathitrust.org/htrc")),
  scalaVersion := "2.13.12",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions"
  ),
  resolvers ++= Seq(
    Resolver.mavenLocal,
    "HTRC Nexus Repository" at "https://nexus.htrc.illinois.edu/repository/maven-public"
  ),
  externalResolvers := Resolver.combineDefaultResolvers(resolvers.value.toVector, mavenCentral = false),
  Compile / packageBin / packageOptions += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  ),
  versionScheme := Some("semver-spec"),
  credentials += Credentials(
    "Sonatype Nexus Repository Manager", // realm
    "nexus.htrc.illinois.edu", // host
    "drhtrc", // user
    sys.env.getOrElse("HTRC_NEXUS_DRHTRC_PWD", "abc123") // password
  )
))

lazy val ammoniteSettings = Seq(
  libraryDependencies +=
    {
      val version = scalaBinaryVersion.value match {
        case "2.10" => "1.0.3"
        case "2.11" => "1.6.7"
        case _ ⇒  "2.5.11"
      }
      "com.lihaoyi" % "ammonite" % version % Test cross CrossVersion.full
    },
  Test / sourceGenerators += Def.task {
    val file = (Test / sourceManaged).value / "amm.scala"
    IO.write(file, """object amm extends App { ammonite.AmmoniteMain.main(args) }""")
    Seq(file)
  }.taskValue,
  connectInput := true,
  outputStrategy := Some(StdoutOutput)
)

lazy val buildInfoSettings = Seq(
  buildInfoOptions ++= Seq(BuildInfoOption.BuildTime),
  buildInfoPackage := "utils",
  buildInfoKeys ++= Seq[BuildInfoKey](
    "gitSha" -> git.gitHeadCommit.value.getOrElse("N/A"),
    "gitBranch" -> git.gitCurrentBranch.value,
    "gitVersion" -> git.gitDescribedVersion.value.getOrElse("N/A"),
    "gitDirty" -> git.gitUncommittedChanges.value,
    "nameWithVersion" -> s"${name.value} ${version.value}"
  )
)

lazy val dockerSettings = Seq(
    Docker / maintainer := "Boris Capitanu <capitanu@illinois.edu>",
    dockerBaseImage := "docker-registry.htrc.indiana.edu/java8",
    dockerExposedPorts := Seq(9000),
    dockerRepository := Some("docker-registry.htrc.indiana.edu"),
    dockerPermissionStrategy := DockerPermissionStrategy.CopyChown,
//    dockerChmodType := DockerChmodType.UserGroupWriteExecute,
    dockerUpdateLatest := true
)

val configureDependencyByPlatform = settingKey[ModuleID]("Dynamically change reference to the jars dependency depending on the platform")
configureDependencyByPlatform := {
  System.getenv.getOrDefault("OS_NAME", System.getProperty("os.name")).toLowerCase match {
    case mac if mac.contains("mac")  => "org.reactivemongo" % "reactivemongo-shaded-native" % "1.1.0-RC6-osx-x86-64" % "runtime"
    case linux if linux.contains("linux") => "org.reactivemongo" % "reactivemongo-shaded-native" % "1.1.0-RC6-linux-x86-64" % "runtime"
    case osName => throw new RuntimeException(s"Unsupported operating system: $osName")
  }
}

lazy val `htrc-metadata-service` = (project in file("."))
  .enablePlugins(PlayScala, BuildInfoPlugin, GitVersioning, GitBranchPrompt, JavaAppPackaging, DockerPlugin)
  .settings(ammoniteSettings)
  .settings(buildInfoSettings)
  .settings(dockerSettings)
  .settings(
    name := "HTRC-MetadataService",
    description := "Service that provides retrieval functionality of metadata records for HathiTrust volumes",
    licenses += "Apache2" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
    maintainer := "capitanu@illinois.edu",
    libraryDependencies ++= Seq(
      guice,
      filters,
      "com.typesafe.play"             %% "play-streams"                     % "2.8.19",
      "org.reactivemongo"             %% "play2-reactivemongo"              % "1.1.0-play28-RC9"
        exclude("org.slf4j", "slf4j-simple"),
      "org.reactivemongo"             %% "reactivemongo-akkastream"         % "1.1.0-RC9",
      configureDependencyByPlatform.value,
      "org.scalatestplus.play"        %% "scalatestplus-play"               % "5.1.0"   % Test
    ),
    routesGenerator := InjectedRoutesGenerator
  )
