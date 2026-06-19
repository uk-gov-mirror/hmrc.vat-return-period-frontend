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
import assets.messages.{AuthMessages, ReturnFrequencyMessages}
import audit.mocks.MockAuditingService
import audit.models.UpdateReturnFrequencyAuditModel
import base.BaseSpec
import common.SessionKeys
import mocks.MockAuth
import mocks.services.{MockCustomerCircumstanceDetailsService, MockReturnFrequencyService}
import models.returnFrequency.{Jan, Monthly}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import views.html.returnFrequency.ConfirmDates

class ConfirmVatDatesControllerSpec extends BaseSpec
  with MockReturnFrequencyService
  with MockAuditingService
  with MockCustomerCircumstanceDetailsService
  with MockAuth {

  val confirmDates: ConfirmDates = injector.instanceOf[ConfirmDates]

  object TestConfirmVatDatesController extends ConfirmVatDatesController(
    mockAuthPredicate,
    errorHandler,
    mockReturnFrequencyService,
    mockCustomerDetailsService,
    mockAuditService,
    mockInFlightReturnPeriodPredicate,
    mockInFlightAnnualAccountingPredicate,
    mcc,
    confirmDates
  )

  "Calling the .show action" when {

    "user is authorised" when {

      "current return frequency is in session" when {

        "new return frequency is in session" when {

          "value is valid" should {

            lazy val result = TestConfirmVatDatesController.show(fakeRequest.withSession(
              SessionKeys.mtdVatvcCurrentReturnFrequency -> "March",
              SessionKeys.mtdVatvcNewReturnFrequency -> "January",
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false"
              )
            )

            lazy val document = Jsoup.parse(contentAsString(result))

            "return 200" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "render the Confirm Dates Page" in {
              document.title shouldBe ReturnFrequencyMessages.ConfirmPage.title
            }
          }

          "value is invalid" should {
            lazy val result = TestConfirmVatDatesController.show(fakeRequest.withSession(
              SessionKeys.mtdVatvcCurrentReturnFrequency -> "March",
              SessionKeys.mtdVatvcNewReturnFrequency -> "Not valid",
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            lazy val document = Jsoup.parse(contentAsString(result))

            "return 500" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "render the ISE page" in {
              document.title shouldBe AuthMessages.problemWithServiceTitle + AuthMessages.mtdfvTitleSuffix
            }
          }
        }

        "new return frequency is not in session" should {

          lazy val result = TestConfirmVatDatesController.show(fakeRequest.withSession(
            SessionKeys.mtdVatvcCurrentReturnFrequency -> "March",
            SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
          )

          "return 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.returnFrequency.routes.ChooseDatesController.show.url}" in {
            redirectLocation(result) shouldBe Some(controllers.returnFrequency.routes.ChooseDatesController.show.url)
          }
        }

        "a value for annual accounting is in session" should {
          
          lazy val result = TestConfirmVatDatesController.show(fakeRequest.withSession(
            SessionKeys.mtdVatvcCurrentReturnFrequency -> "March",
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
            Jsoup.parse(contentAsString(result)).title() shouldBe "You already have a change pending - Manage your VAT account - GOV.UK"
          }
        }
      }

      "current return frequency is not in session" should {

        lazy val result = TestConfirmVatDatesController.show(fakeRequest)

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          mockCustomerDetailsSuccess(circumstanceDetailsNoPending)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.returnFrequency.routes.ChooseDatesController.show.url}" in {
          redirectLocation(result) shouldBe Some(controllers.returnFrequency.routes.ChooseDatesController.show.url)
        }
      }
    }

    "the user is insolvent and not continuing to trade" should {

      "return 403 (Forbidden)" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        val result = TestConfirmVatDatesController.show(insolventRequest)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    authControllerChecks(TestConfirmVatDatesController.show, fakeRequest)
  }

  "Calling the .submit action" when {

    "user is authorised" when {

      "current return frequency is in session" when {

        "new return frequency is in session" when {

          "updateReturnFrequency returns an unexpected error" should {

            lazy val result = TestConfirmVatDatesController.submit(fakeRequest.withSession(
              SessionKeys.mtdVatvcNewReturnFrequency -> "Monthly",
              SessionKeys.mtdVatvcCurrentReturnFrequency -> "January",
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return 500" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              setupMockCustomerDetails(vrn)(Right(circumstanceDetailsNoPending))
              setupMockReturnFrequencyServiceWithFailure()
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
              messages(Jsoup.parse(contentAsString(result)).title) shouldBe
                AuthMessages.problemWithServiceTitle + AuthMessages.mtdfvTitleSuffix
            }
          }

          "updateReturnFrequency returns a 409 error (stagger change already in progress)" should {

            lazy val result = TestConfirmVatDatesController.submit(fakeRequest.withSession(
              SessionKeys.mtdVatvcNewReturnFrequency -> "Monthly",
              SessionKeys.mtdVatvcCurrentReturnFrequency -> "January",
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return 303" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              setupMockCustomerDetails(vrn)(Right(circumstanceDetailsNoPending))
              setupMockReturnFrequencyServiceWithConflict()
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the manage VAT overview page" in {
              redirectLocation(result) shouldBe Some(mockAppConfig.manageVatUrl)
            }
          }

          "updateReturnFrequency returns success" should {

            lazy val result = TestConfirmVatDatesController.submit(fakeRequest.withSession(
              SessionKeys.mtdVatvcNewReturnFrequency -> "January",
              SessionKeys.mtdVatvcCurrentReturnFrequency -> "Monthly",
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return 303" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              setupMockReturnFrequencyServiceWithSuccess()
              setupMockCustomerDetails(vrn)(Right(circumstanceDetailsNoPending))
              setupAndVerifyAuditEvent(
                UpdateReturnFrequencyAuditModel(user, Monthly, Jan, Some(partyType)),
                Some(routes.ConfirmationController.show.url)
              )
              status(result) shouldBe Status.SEE_OTHER
            }

            s"redirect to ${controllers.returnFrequency.routes.ConfirmationController.show.url}" in {
              redirectLocation(result) shouldBe
                Some(controllers.returnFrequency.routes.ConfirmationController.show.url)
            }
          }

          "getCustomerCircumstanceDetails returns an unexpected error" should {

            lazy val result = TestConfirmVatDatesController.submit(fakeRequest.withSession(
              SessionKeys.mtdVatvcNewReturnFrequency -> "Monthly",
              SessionKeys.mtdVatvcCurrentReturnFrequency -> "January",
              SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
            )

            "return 500" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              mockCustomerDetailsError()
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
              messages(Jsoup.parse(contentAsString(result)).title) shouldBe
                AuthMessages.problemWithServiceTitle + AuthMessages.mtdfvTitleSuffix
            }
          }
        }

        "new return frequency is not in session" should {

          lazy val result = TestConfirmVatDatesController.submit(fakeRequest.withSession(
            SessionKeys.mtdVatvcNewReturnFrequency -> "January",
            SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
          )

          "return 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.returnFrequency.routes.ChooseDatesController.show.url}" in {
            redirectLocation(result) shouldBe Some(controllers.returnFrequency.routes.ChooseDatesController.show.url)
          }
        }

        "session value is invalid" should {
          lazy val result = TestConfirmVatDatesController.submit(fakeRequest.withSession(
            SessionKeys.mtdVatvcCurrentReturnFrequency -> "March",
            SessionKeys.mtdVatvcNewReturnFrequency -> "Not valid",
            SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
          )

          lazy val document = Jsoup.parse(contentAsString(result))

          "return 500" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }

          "render the ISE page" in {
            document.title shouldBe AuthMessages.problemWithServiceTitle + AuthMessages.mtdfvTitleSuffix
          }
        }
      }

      "user has an in-flight annual accounting change" should {

        lazy val result = TestConfirmVatDatesController.show(fakeRequest.withSession(
          SessionKeys.mtdVatvcCurrentReturnFrequency -> "Jan" )
        )

        "return OK (200)" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          mockCustomerDetailsSuccess(circumstanceDetailsModelMaxAA)
          status(result) shouldBe Status.OK
        }

        s"have the correct page title" in {
          Jsoup.parse(contentAsString(result)).title shouldBe "You already have a change pending - Manage your VAT account - GOV.UK"
        }

        "add the current return frequency to the session" in {
          session(result).get(SessionKeys.mtdVatvcCurrentAnnualAccounting) shouldBe Some("true")
        }
      }

      "current return frequency is not in session" should {

        lazy val result = TestConfirmVatDatesController.submit(fakeRequest.withSession(
          SessionKeys.mtdVatvcCurrentReturnFrequency -> "January",
          SessionKeys.mtdVatvcCurrentAnnualAccounting -> "false")
        )

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.returnFrequency.routes.ChooseDatesController.show.url}" in {
          redirectLocation(result) shouldBe Some(controllers.returnFrequency.routes.ChooseDatesController.show.url)
        }
      }
    }

    "the user is insolvent and not continuing to trade" should {

      "return 403 (Forbidden)" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        val result = TestConfirmVatDatesController.submit(insolventRequest)
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
