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

package controllers.returnFrequency

import assets.CircumstanceDetailsTestConstants._
import assets.ReturnPeriodTestConstants._
import assets.messages.{AuthMessages, ReturnFrequencyMessages}
import audit.mocks.MockAuditingService
import audit.models.StartJourneyAuditModel
import base.BaseSpec
import common.SessionKeys
import common.SessionKeys.insolventWithoutAccessKey
import mocks.MockAuth
import mocks.services.MockCustomerCircumstanceDetailsService
import models.returnFrequency.Jan
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.returnFrequency.ChooseDates

class ChooseDatesControllerSpec extends BaseSpec with MockCustomerCircumstanceDetailsService
  with MockAuth with MockAuditingService {

  implicit val chooseDatesView: ChooseDates = injector.instanceOf[ChooseDates]

  object TestChooseDatesController extends ChooseDatesController (
    mockAuthPredicate,
    mockInFlightReturnPeriodPredicate,
    mockInFlightAnnualAccountingPredicate,
    errorHandler,
    mcc,
    mockAuditService
  )

  "ChooseDatesController 'show' method" when {

    "the user is authorised" when {

      "user has an in-flight return frequency change" should {

        lazy val result = TestChooseDatesController.show(fakeRequest)

        "return SEE_OTHER (303)" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          mockCustomerDetailsSuccess(circumstanceDetailsModelMax)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${mockAppConfig.manageVatUrl}" in {
          redirectLocation(result) shouldBe Some(mockAppConfig.manageVatUrl)
        }
      }

      "user has an in-flight annual accounting change" should {

        lazy val result = TestChooseDatesController.show(fakeRequest.withSession(
          SessionKeys.mtdVatvcCurrentReturnFrequency -> returnPeriodJan)
        )

        "return OK (200)" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          mockCustomerDetailsSuccess(circumstanceDetailsModelMaxAA)
          status(result) shouldBe Status.OK
        }

        s"have the correct page title" in {
          Jsoup.parse(contentAsString(result)).title shouldBe
            "You already have a change pending - Manage your VAT account - GOV.UK"
        }

        "add the current return frequency to the session" in {
          session(result).get(SessionKeys.mtdVatvcCurrentAnnualAccounting) shouldBe Some("true")
        }
      }

      "user does not have an in-flight change" when {

        "a value is not held in session for the current Return Frequency" should {

          lazy val result = TestChooseDatesController.show(fakeRequest)

          "return SEE_OTHER (303)" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockCustomerDetailsSuccess(circumstanceDetailsNoPending)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.returnFrequency.routes.ChooseDatesController.show.url}" in {
            redirectLocation(result) shouldBe Some(controllers.returnFrequency.routes.ChooseDatesController.show.url)
          }

          "add the current return frequency to the session" in {
            session(result).get(SessionKeys.mtdVatvcCurrentReturnFrequency) shouldBe Some(returnPeriodMonthly)
          }
        }

        "a value is already held in session for the current Return Frequency" when {

          "a value for new return frequency is not in session" should {

            lazy val result = TestChooseDatesController.show(fakeRequest.withSession(
              SessionKeys.mtdVatvcCurrentReturnFrequency -> returnPeriodJan,
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return OK (200)" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              setupAndVerifyAuditEvent(StartJourneyAuditModel(user, Jan), Some(routes.ChooseDatesController.show.url))
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            s"have the title ${ReturnFrequencyMessages.ChoosePage.title}" in {
              Jsoup.parse(contentAsString(result)).title() shouldBe ReturnFrequencyMessages.ChoosePage.title
            }
          }

          "a value for annual accounting is in session" should {

            lazy val result = TestChooseDatesController.show(fakeRequest.withSession(
              SessionKeys.mtdVatvcCurrentReturnFrequency -> returnPeriodJan,
              SessionKeys.mtdVatvcNewReturnFrequency -> returnPeriodMar,
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "true")
            )

            "return OK (200)" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            s"have the title ${ReturnFrequencyMessages.ChoosePage.title}" in {
              Jsoup.parse(contentAsString(result)).title() shouldBe
                "You already have a change pending - Manage your VAT account - GOV.UK"
            }
          }

          "a value for new return frequency is in session" should {

            lazy val result = TestChooseDatesController.show(fakeRequest.withSession(
              SessionKeys.mtdVatvcCurrentReturnFrequency -> returnPeriodJan,
              SessionKeys.mtdVatvcNewReturnFrequency -> returnPeriodMar,
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return OK (200)" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              setupAndVerifyAuditEvent(StartJourneyAuditModel(user, Jan), Some(routes.ChooseDatesController.show.url))
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "have the January radio option selected" in {
              Jsoup.parse(contentAsString(result)).select("#period-option-march").attr("checked") shouldBe ""
            }

            s"have the title ${ReturnFrequencyMessages.ChoosePage.title}" in {
              Jsoup.parse(contentAsString(result)).title() shouldBe ReturnFrequencyMessages.ChoosePage.title
            }
          }
        }

        "a return frequency is NOT returned from the call to get circumstance info" should {

          lazy val result = TestChooseDatesController.show(fakeRequest)

          "return 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockCustomerDetailsSuccess(circumstanceDetailsModelMin)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${mockAppConfig.manageVatUrl}" in {
            redirectLocation(result) shouldBe Some(mockAppConfig.manageVatUrl)
          }
        }

        "an error is returned from Customer Details" should {

          lazy val request = FakeRequest("GET", "/").withFormUrlEncodedBody(("period-option", ""))
            .withSession(insolventWithoutAccessKey -> "false")
          lazy val result = TestChooseDatesController.show(request.withSession(
            SessionKeys.mtdVatvcCurrentReturnFrequency -> "invalid",
            SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
          )

          "return Internal Server Error (500)" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            messages(Jsoup.parse(contentAsString(result)).title) shouldBe
              AuthMessages.problemWithServiceTitle + AuthMessages.mtdfvTitleSuffix
          }
        }
      }
    }

    "the user is insolvent and not continuing to trade" should {

      "return 403 (Forbidden)" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        lazy val result = TestChooseDatesController.show(insolventRequest)
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "ChooseDatesController 'submit' method" when {

    "user is authorised" when {

      "user has an in-flight return frequency change" should {

        lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("period-option", "January"))
          .withSession(insolventWithoutAccessKey -> "false")
        lazy val result = TestChooseDatesController.submit(request)

        "return SEE_OTHER (303)" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          mockCustomerDetailsSuccess(circumstanceDetailsModelMax)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${mockAppConfig.manageVatUrl}" in {
          redirectLocation(result) shouldBe Some(mockAppConfig.manageVatUrl)
        }
      }

      "user does not have an in-flight change" when {

        "a value is not held in session for the current Return Frequency" should {

          lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("period-option", "January"))
            .withSession(insolventWithoutAccessKey -> "false")
          lazy val result = TestChooseDatesController.submit(request)

          "return SEE_OTHER (303)" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockCustomerDetailsSuccess(circumstanceDetailsNoPending)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.returnFrequency.routes.ChooseDatesController.show.url}" in {
            redirectLocation(result) shouldBe Some(controllers.returnFrequency.routes.ChooseDatesController.show.url)
          }

          "add the current return frequency to the session" in {
            session(result).get(SessionKeys.mtdVatvcCurrentReturnFrequency) shouldBe Some(returnPeriodMonthly)
          }
        }

        "submitting with an option selected" should {

          lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("period-option", "January"))
            .withSession(insolventWithoutAccessKey -> "false")
          lazy val result = TestChooseDatesController.submit(request.withSession(
            SessionKeys.mtdVatvcCurrentReturnFrequency-> returnPeriodJan,
            SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
          )

          "return 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.returnFrequency.routes.ConfirmVatDatesController.show.url}" in {
            redirectLocation(result) shouldBe Some(controllers.returnFrequency.routes.ConfirmVatDatesController.show.url)
          }

          "add the new return frequency to the session" in {
            session(result).get(SessionKeys.mtdVatvcNewReturnFrequency) shouldBe Some(returnPeriodJan)
          }
        }

        "submitting with no option selected" should {

          "current return period in session is not valid" should {

            lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("period-option", ""))
              .withSession(insolventWithoutAccessKey -> "false")
            lazy val result = TestChooseDatesController.submit(request.withSession(
              SessionKeys.mtdVatvcCurrentReturnFrequency -> "invalid",
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return Internal Server Error (500)" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
              messages(Jsoup.parse(contentAsString(result)).title) shouldBe
                AuthMessages.problemWithServiceTitle + AuthMessages.mtdfvTitleSuffix
            }
          }

          "current return period in session is valid" should {

            lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("period-option", ""))
              .withSession(insolventWithoutAccessKey -> "false")
            lazy val result = TestChooseDatesController.submit(request.withSession(
              SessionKeys.mtdVatvcCurrentReturnFrequency -> returnPeriodJan,
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return Bad Request (400)" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              status(result) shouldBe Status.BAD_REQUEST
            }

            s"have the title ${ReturnFrequencyMessages.ChoosePage.errorTitle}" in {
              Jsoup.parse(contentAsString(result)).title() shouldBe ReturnFrequencyMessages.ChoosePage.errorTitle
            }
          }
        }
      }
    }

    "the user is insolvent and not continuing to trade" should {

      "return 403 (Forbidden)" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        val result = TestChooseDatesController.submit(insolventRequest)
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
