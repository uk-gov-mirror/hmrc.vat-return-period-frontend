/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appDependencies: Seq[ModuleID] = compile ++ test
val appName = "vat-return-period-frontend"
lazy val plugins: Seq[Plugins] = Seq.empty

val bootstrapPlayVersion = "10.4.0"
val playFrontendHmrcVersion = "12.32.0"

lazy val coverageSettings: Seq[Setting[?]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    "testOnly.*",
    "app.*",
    "common.*",
    "config.*",
    "testOnlyDoNotUseInAppConf.*",
    "prod.*",
    "views.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

val compile = Seq(
  play.sbt.PlayImport.ws,
  "uk.gov.hmrc"    %% "bootstrap-frontend-play-30"    % bootstrapPlayVersion,
  "uk.gov.hmrc"    %% "play-frontend-hmrc-play-30"    % playFrontendHmrcVersion
)

val test = Seq(
  "uk.gov.hmrc"             %% "bootstrap-test-play-30"       % bootstrapPlayVersion,
  "org.scalamock"           %% "scalamock"                    % "7.5.1"
).map(_ % s"$Test")

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._"
)

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.18"

lazy val microservice: Project = Project(appName, file("."))
  .enablePlugins((Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins) *)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9167)
  .settings(coverageSettings *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    Test / Keys.fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    scalacOptions ++= Seq(
      "-Wconf:cat=unused-imports&site=.*views.html.*:s",
      "-Wconf:cat=unused-imports&src=routes/.*:s"
    ),
    RoutesKeys.routesImport += "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
