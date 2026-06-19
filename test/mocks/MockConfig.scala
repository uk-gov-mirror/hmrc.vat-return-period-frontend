/*
 * Copyright 2026 HM Revenue & Customs
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

package mocks

import config.AppConfig
import config.features.Features
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call

class MockConfig(implicit val runModeConfiguration: Configuration) extends AppConfig {

  override lazy val reportAProblemPartialUrl: String = ""
  override lazy val reportAProblemNonJSUrl: String = ""
  override lazy val betaFeedbackUrl: String = "feedback-url"
  override lazy val betaFeedbackUnauthenticatedUrl: String = ""
  override val features: Features = new Features()
  override lazy val signInUrl: String = "sign-in-url"
  override def signOutUrl(identifier: String): String = s"/some-gg-signout-url/$identifier"
  override def exitSurveyUrl(identifier: String): String = s"/some-survey-url/$identifier"
  override lazy val timeoutPeriod: Int = 1800
  override lazy val timeoutCountdown: Int = 20
  override lazy val unauthorisedSignOutUrl: String = "/unauth-signout-url"
  override lazy val agentClientLookupStartUrl: String => String = uri => s"agent-client-lookup-start-url/$uri"
  override lazy val agentClientUnauthorisedUrl: String => String = uri => s"agent-client-unauthorised-url/$uri"
  override lazy val agentClientLookupUrl: String = "/client-vat-account"
  override lazy val changeClientUrl: String = "/change-client"
  override lazy val govUkGuidanceMtdVat: String = "mtd-vat"
  override lazy val govUkGuidanceAgentServices: String = "agent-services"
  override lazy val manageVatUrl: String = "/manage-vat"
  override val gtmContainer: String = "x"
  override val routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )
  override val vatSubscriptionBaseURL: String = "http://localhost"
  override val vatSubscriptionDynamicStubURL: String = "http://localhost"
  override val contactPreferenceURL: String = "http://localhost"
  override lazy val vatDetailsUrl: String = "vat-details-url"
  override lazy val btaHomeUrl: String = "bta-home"
  override lazy val urBannerBaseUrl: String = "ur-banner-url"
}