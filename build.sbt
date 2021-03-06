name in ThisBuild := "youi-designer"
organization in ThisBuild := "io.youi"
version in ThisBuild := "1.0.0"
scalaVersion in ThisBuild := "2.12.6"
resolvers in ThisBuild += Resolver.sonatypeRepo("releases")

val psdVersion = "3.2.0.1"
val hasherVersion = "1.2.1"
val media4sVersion = "1.0.12"

lazy val designer = crossApplication.in(file("."))
  .settings(
    youiVersion := "0.9.0-SNAPSHOT"
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.outr" %%% "psd-scala-js" % psdVersion
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.matthicks" %% "media4s" % media4sVersion,
      "com.outr" %% "hasher" % hasherVersion
    )
  )

lazy val designerJS = designer.js
lazy val designerJVM = designer.jvm
