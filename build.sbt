def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

lazy val ofdm = (project in file("ofdm"))

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel-iotesters" -> "1.3.+",
  "chisel-testers2" -> "0.1.+",
  "dsptools" -> "1.2.+"
  )

lazy val espChisel = (project in file("."))
  .settings(
    name := "esp-chisel-accelerators",
    version := "1.0.0",
    scalaVersion := "2.12.10",
    crossScalaVersions := Seq("2.11.12", "2.12.10"),
    libraryDependencies ++= Seq("chisel-iotesters", "chisel-testers2", "dsptools")
      .map { dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep)) },
    libraryDependencies += "com.thoughtworks.xstream" % "xstream" % "1.4.11.1",
    scalacOptions ++= scalacOptionsVersion(scalaVersion.value) ++
      Seq("-unchecked", "-deprecation", "-Ywarn-unused-import"),
    javacOptions ++= javacOptionsVersion(scalaVersion.value))
  .dependsOn(ofdm)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)
