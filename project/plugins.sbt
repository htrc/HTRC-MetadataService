logLevel := Level.Warn

addSbtPlugin("com.typesafe.play"      % "sbt-plugin"          % "2.6.15")
addSbtPlugin("com.typesafe.sbt"       % "sbt-git"             % "1.0.0")
addSbtPlugin("com.typesafe.sbt"       % "sbt-native-packager" % "1.3.4")
addSbtPlugin("com.eed3si9n"           % "sbt-assembly"        % "0.14.6")
addSbtPlugin("com.eed3si9n"           % "sbt-buildinfo"       % "0.9.0")
addSbtPlugin("org.wartremover"        % "sbt-wartremover"     % "2.2.1")
addSbtPlugin("com.jsuereth"           % "sbt-pgp"             % "1.1.1")