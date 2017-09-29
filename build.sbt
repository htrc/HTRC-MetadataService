import play.sbt.routes.RoutesKeys
import sbtbuildinfo.BuildInfoPlugin
import com.typesafe.sbt.packager.docker._

showCurrentGitBranch

git.useGitDescribe := true

lazy val commonSettings = Seq(
  organization := "org.hathitrust.htrc",
  organizationName := "HathiTrust Research Center",
  organizationHomepage := Some(url("https://www.hathitrust.org/htrc")),
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq(
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-target:jvm-1.8"
  ),
  resolvers ++= Seq(
    "I3 Repository" at "http://nexus.htrc.illinois.edu/content/groups/public",
    Resolver.mavenLocal
  ),
  buildInfoOptions ++= Seq(BuildInfoOption.BuildTime),
  buildInfoPackage := "utils",
  buildInfoKeys ++= Seq[BuildInfoKey](
    "gitSha" -> git.gitHeadCommit.value.getOrElse("N/A"),
    "gitBranch" -> git.gitCurrentBranch.value,
    "gitVersion" -> git.gitDescribedVersion.value.getOrElse("N/A"),
    "gitDirty" -> git.gitUncommittedChanges.value
  ),
  packageOptions in(Compile, packageBin) += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  )
)

lazy val dockerSettings = Seq(
    maintainer in Docker := "Boris Capitanu <capitanu@illinois.edu>",
    dockerBaseImage := "docker-registry.htrc.indiana.edu/java8",
    dockerExposedPorts := Seq(9000),
    dockerRepository := Some("docker-registry.htrc.indiana.edu"),
    dockerUpdateLatest := true
)

lazy val `htrc-metadata-service` = (project in file("."))
  .enablePlugins(PlayScala, BuildInfoPlugin, GitVersioning, GitBranchPrompt, JavaAppPackaging, DockerPlugin)
  .settings(commonSettings)
  .settings(dockerSettings)
  .settings(
    name := "HTRC-MetadataService",
    routesGenerator := InjectedRoutesGenerator,
    RoutesKeys.routesImport += "play.modules.reactivemongo.PathBindables._",
    libraryDependencies ++= Seq(
      filters,
      "org.hathitrust.htrc"           %% "pairtree-helper"      % "3.2",
      "net.codingwell"                %% "scala-guice"          % "4.1.0",
      "org.reactivemongo"             %% "play2-reactivemongo"  % "0.12.3",
      "org.scalatestplus.play"        %% "scalatestplus-play"   % "1.5.1" % Test
    )
  )


