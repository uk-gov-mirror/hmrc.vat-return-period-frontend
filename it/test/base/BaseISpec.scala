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

package base

import config.AppConfig
import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.HeaderNames
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.libs.ws.WSBodyWritables._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.{Application, Environment, Mode}
import stubs.AuthStub
import uk.gov.hmrc.http.client.HttpClientV2
import utils.WireMockHelper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.test.Helpers.{await, defaultAwaitTimeout}

trait BaseISpec extends AnyWordSpecLike
  with WireMockHelper
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with GuiceOneServerPerSuite
  with GivenWhenThen {

  def servicesConfig: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.vat-subscription.host" -> WireMockHelper.wireMockHost,
    "microservice.services.vat-subscription.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.auth.host" -> WireMockHelper.wireMockHost,
    "microservice.services.auth.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.contact-preferences.host" -> WireMockHelper.wireMockHost,
    "microservice.services.contact-preferences.port" -> WireMockHelper.wireMockPort.toString,
    "features.stubContactPreferences.enabled" -> "false"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  lazy val httpClient: HttpClientV2 = app.injector.instanceOf[HttpClientV2]
  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val appRouteContext: String = "/vat-through-software/account/returns"
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val vrn = "999999999"

  def authSession: Map[String, String] = Map("authToken"-> "mock-bearer-token")

  override def beforeAll(): Unit = {
    super.beforeAll()
    startServer()
  }

  override def afterAll(): Unit = {
    stopServer()
    super.afterAll()
  }

  class PreconditionBuilder {
    implicit val builder: PreconditionBuilder = this
    def user: User = new User()
    def agent: Agent = new Agent()
  }

  def assuming: PreconditionBuilder = new PreconditionBuilder

  class User()(implicit builder: PreconditionBuilder) {
    def isAuthenticated: PreconditionBuilder = {
      Given("I stub a User who successfully signed up to MTD VAT")
      AuthStub.authorised()
      builder
    }

    def isNotSignedUpToMtdVat: PreconditionBuilder = {
      Given("I stub a User who is not signed up to MTD VAT")
      AuthStub.unauthorised()
      builder
    }
  }

  class Agent()(implicit builder: PreconditionBuilder) {
    def isSignedUpToAgentServices: PreconditionBuilder = {
      Given("I stub an Agent successfully signed up to Agent Services")
      AuthStub.agentAuthorised()
      builder
    }

    def isNotSignedUpToAgentServices: PreconditionBuilder = {
      Given("I stub an Agent who is NOT signed up to Agent Services")
      AuthStub.agentUnauthorised()
      builder
    }
  }

  def get(path: String, additionalCookies: Map[String, String] = Map.empty): WSResponse = await(
    buildRequest(path, additionalCookies ++ authSession).get()
  )

  def postJSValueBody(path: String, additionalCookies: Map[String, String] = Map.empty)(body: JsValue): WSResponse = await(
    buildRequest(path, additionalCookies ++ authSession).post(body)
  )

  def buildRequest(path: String, additionalCookies: Map[String, String] = Map.empty): WSRequest =
    wsClient.url(s"http://localhost:$port$appRouteContext$path")
      .withHttpHeaders(HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(additionalCookies), "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)

}
