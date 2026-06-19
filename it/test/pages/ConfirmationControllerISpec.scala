/*
 * Copyright 2023 HM Revenue & Customs
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

package pages

import play.api.i18n.Messages
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import stubs.VatSubscriptionStub
import stubs.VatSubscriptionStub._

class ConfirmationControllerISpec extends BasePageISpec{

  val path = "/confirmation-vat-return-dates"

  "Calling .show" when {

    "the user is a Principle Entity and not an Agent" when {

      def show(sessionVrn: Option[String] = None): WSResponse = get(path, formatSessionVrn(sessionVrn))

      "authorised" should {
        "render the return frequency confirmation page" in {

          assuming.user.isAuthenticated
          VatSubscriptionStub.getClientDetailsSuccess("999999999")(circumstanceDetailsJsonMax)

          When("I call to show the Confirm Return Frequency Page")
          val res = show()

          res should have(
            httpStatus(OK),
            elementText("#page-heading")(Messages("received_frequency.heading"))
          )
        }
      }

      "unauthorised" should {

        "render unauthorised page" in {

          assuming.user.isNotSignedUpToMtdVat

          When("I call to show the Confirm Return Frequency Page")
          val res = show()

          res should have(
            httpStatus(FORBIDDEN),
            pageTitle("You can’t use this service yet" + titleSuffixOther)
          )
        }
      }
    }

    "the user is an Agent" when {

      def show(sessionVrn: Option[String] = None): WSResponse = get(path, formatSessionVrn(sessionVrn))

      "a valid VRN is being stored" should {

        "render the return frequency confirmation page" in {

          assuming.agent.isSignedUpToAgentServices

          And("I stub a successful response from VAT Subscription")
          getClientDetailsSuccess("999999999")(circumstanceDetailsJsonMax)

          When("I call to show the Confirm Return Frequency Page")
          val res = show(Some("999999999"))

          res should have(
            httpStatus(OK),
            elementText("#page-heading")(Messages("received_frequency.heading"))
          )
        }
      }

      "no VRN is being stored" should {

        "redirect to Enter Client VRN page" in {

          assuming.agent.isSignedUpToAgentServices

          When("I call to show the Confirm Return Frequency Page")
          val res = show()

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(appConfig.agentClientLookupStartUrl(controllers.returnFrequency.routes.ChooseDatesController.show.url))
          )
        }
      }

      "who is unauthorised" should {

        "render the unauthorised error page" in {

          assuming.agent.isNotSignedUpToAgentServices

          When("I call to show the Confirm Return Frequency Page")
          val res = show()

          res should have(
            httpStatus(FORBIDDEN),
            pageTitle("You can’t use this service yet" + titleSuffixOther)
          )
        }
      }
    }
  }
}
