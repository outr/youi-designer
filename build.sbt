name in ThisBuild := "youi-designer"
organization in ThisBuild := "io.youi"
version in ThisBuild := "1.0.0"
scalaVersion in ThisBuild := "2.12.3"
resolvers in ThisBuild += Resolver.sonatypeRepo("releases")

val psdVersion = "3.2.0.1"

lazy val designer = crossApplication.in(file("."))
  .settings(
    youiVersion := "0.6.5-SNAPSHOT"
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.outr" %%% "psd-scala-js" % psdVersion
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.matthicks" %% "media4s" % "1.0.12"
    )
  )

lazy val designerJS = designer.js
lazy val designerJVM = designer.jvm
