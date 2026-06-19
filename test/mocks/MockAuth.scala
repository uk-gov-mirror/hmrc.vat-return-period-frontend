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

import _root_.services.EnrolmentsAuthService
import base.BaseSpec
import controllers.predicates.{AuthPredicate, InFlightAnnualAccountingPredicate, InFlightReturnFrequencyPredicate}
import mocks.services.MockCustomerCircumstanceDetailsService
import play.api.http.Status
import play.api.mvc.{Action, AnyContent, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.annualAccounting.PreventLeaveAnnualAccounting
import views.html.errors.{UnauthorisedAgent, UnauthorisedNonAgent, UserInsolventError}

import scala.concurrent.{ExecutionContext, Future}

trait MockAuth extends BaseSpec with MockCustomerCircumstanceDetailsService {

  val unauthorisedAgentView: UnauthorisedAgent = injector.instanceOf[UnauthorisedAgent]
  val unauthorisedNonAgentView: UnauthorisedNonAgent = injector.instanceOf[UnauthorisedNonAgent]
  val userInsolventErrorView: UserInsolventError = injector.instanceOf[UserInsolventError]

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]
  lazy val mockEnrolmentsAuthService: EnrolmentsAuthService = new EnrolmentsAuthService(mockAuthConnector)
  lazy val mockAuthPredicate: AuthPredicate = new AuthPredicate(mockEnrolmentsAuthService,
    errorHandler, mockCustomerDetailsService, mockAppConfig, mcc, unauthorisedAgentView, unauthorisedNonAgentView, userInsolventErrorView)

  private def mockAuthoriseCall[A](authResponse: Future[A]): Unit =
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[A])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(authResponse)

  def mockAuthorise(authResponse: Future[~[Option[AffinityGroup], Enrolments]]): Unit =
    mockAuthoriseCall(authResponse)

  def mockAuthoriseAsAgent(firstAuthResponse: Future[~[Option[AffinityGroup], Enrolments]],
                           secondAuthResponse: Future[Enrolments]): Unit = {
    mockAuthoriseCall(firstAuthResponse)
    mockAuthoriseCall(secondAuthResponse)
  }

  def authControllerChecks(action: Action[AnyContent], request: Request[AnyContent]): Unit = {

    "user is unauthenticated" should {

      lazy val result = action(request)

      "return 303" in {
        mockAuthorise(Future.failed(BearerTokenExpired()))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to sign-in" in {
        redirectLocation(result) shouldBe Some(mockAppConfig.signInUrl)
      }
    }

    "user is unauthorised" should {

      lazy val result = action(request)

      "return 403" in {
        mockAuthorise(Future.successful(new ~(Some(Individual), otherEnrolment)))
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  private def createEnrolment(key: String,
                              identifierKey: String,
                              identifierValue: String,
                              delegatedAuthRule: Option[String] = None) = Enrolments(
    Set(
      Enrolment(
        key,
        Seq(EnrolmentIdentifier(identifierKey, identifierValue)),
        "Activated",
        delegatedAuthRule
      )
    )
  )

  val agentServicesEnrolment: Enrolments = createEnrolment("HMRC-AS-AGENT", "AgentReferenceNumber", "XAIT1234567", Some("mtd-vat-auth"))
  val unauthorisedEnrolment: Enrolments = createEnrolment("", "AgentReferenceNumber", "XAIT1234567", Some("mtd-vat-auth"))
  val mtdVatEnrolment: Enrolments = createEnrolment("HMRC-MTD-VAT", "VRN", "999999999")
  val otherEnrolment: Enrolments = createEnrolment("", "BLAH", "12345")
  val mtdVatAuthorisedResponse: Future[~[Option[AffinityGroup], Enrolments]] = Future.successful(new ~(Some(Individual), mtdVatEnrolment))
  val agentAuthorisedResponse: Future[~[Option[AffinityGroup], Enrolments]] = Future.successful(new ~(Some(Agent), agentServicesEnrolment))
  val agentServicesEnrolmentWithoutDelegatedAuth: Enrolments = createEnrolment("HMRC-AS-AGENT", "AgentReferenceNumber", "XAIT1234567", None)

  val mockInFlightReturnPeriodPredicate: InFlightReturnFrequencyPredicate =
    new InFlightReturnFrequencyPredicate(
      mockCustomerDetailsService,
      errorHandler,
      messagesApi,
      mockAppConfig,
      mcc
    )

  val preventLeaveAnnualAccountingView: PreventLeaveAnnualAccounting = injector.instanceOf[PreventLeaveAnnualAccounting]

  val mockInFlightAnnualAccountingPredicate: InFlightAnnualAccountingPredicate =
    new InFlightAnnualAccountingPredicate(
      mockCustomerDetailsService,
      errorHandler,
      mockAppConfig,
      ec,
      messagesApi,
      preventLeaveAnnualAccountingView
    )
}
