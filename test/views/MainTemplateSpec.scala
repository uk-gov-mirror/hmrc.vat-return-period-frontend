/*
 * Copyright 2024 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.MainTemplate

class MainTemplateSpec extends ViewBaseSpec {

  val injectedView: MainTemplate = injector.instanceOf[MainTemplate]

  object Selectors {
    val serviceNameSelector = ".govuk-service-navigation__link"
    val serviceNameNoUserTypeSelector = ".govuk-service-navigation__text"
  }

  "The MainTemplate" when {

    "the user is an agent" should {

      lazy val view = injectedView("", None, user = Some(agentUser))(Html(""))(agentUser, messages, mockAppConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct dynamic service name" in {
        elementText(Selectors.serviceNameSelector) shouldBe "Your client’s VAT details"
      }

      "have the correct header URL" in {
        element(Selectors.serviceNameSelector).attr("href") shouldBe mockAppConfig.agentClientLookupUrl
      }

    }

    "the user is not an agent" should {

      lazy val view = injectedView("", None, user = Some(user))(Html(""))(user, messages, mockAppConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct dynamic service name" in {
        elementText(Selectors.serviceNameSelector) shouldBe "Manage your VAT account"
      }

      "have the correct header URL" in {
        element(Selectors.serviceNameSelector).attr("href") shouldBe mockAppConfig.vatDetailsUrl
      }

    }

    "the user type cannot be determined" should {

      lazy val view = injectedView("", None)(Html(""))(fakeRequest, messages, mockAppConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the correct dynamic service name" in {
        elementText(Selectors.serviceNameNoUserTypeSelector) shouldBe "VAT"
      }

      "have the correct header URL" in {
        element(Selectors.serviceNameNoUserTypeSelector).attr("href") shouldBe ""
      }

    }
  }
}
