logLevel := Level.Warn

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("com.typesafe.play"      % "sbt-plugin"          % "2.8.19")

addSbtPlugin("com.github.sbt"         % "sbt-git"             % "2.0.1")
addSbtPlugin("com.github.sbt"         % "sbt-native-packager" % "1.9.16")
addSbtPlugin("com.eed3si9n"           % "sbt-assembly"        % "2.1.1")
addSbtPlugin("com.github.sbt"         % "sbt-pgp"             % "2.2.1")
addSbtPlugin("com.github.sbt"         % "sbt-dynver"          % "5.0.1")
addSbtPlugin("org.scoverage"          % "sbt-scoverage"       % "2.0.8")
addSbtPlugin("com.eed3si9n"           % "sbt-buildinfo"       % "0.11.0")
