name in ThisBuild := "youi-designer"
organization in ThisBuild := "io.youi"
version in ThisBuild := "1.0.0"
scalaVersion in ThisBuild := "2.12.3"

val psdVersion = "3.2.0"

lazy val designer = crossApplication.in(file("."))
  .settings(
    youiVersion := "0.6.0-SNAPSHOT"
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.outr" %%% "psd-scala-js" % psdVersion
    )
  )

lazy val designerJS = designer.js
lazy val designerJVM = designer.jvm
