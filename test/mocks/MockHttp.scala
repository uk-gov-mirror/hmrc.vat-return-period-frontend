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

import izumi.reflect.Tag as IzumiTag
import org.scalamock.handlers.CallHandler2
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import play.api.libs.ws.BodyWritable
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import org.scalatest.wordspec.AnyWordSpecLike

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait MockHttp extends AnyWordSpecLike with MockFactory {

  val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

  def mockHttpClientV2Get(url: URL): CallHandler2[URL, HeaderCarrier, RequestBuilder] =
    (mockHttp
      .get(_: URL)(_: HeaderCarrier))
      .expects(url, *)
      .returning(mockRequestBuilder)

  def mockHttpClientV2Execute[O: HttpReads](response: O): CallHandler2[HttpReads[O], ExecutionContext, Future[O]] =
    (mockRequestBuilder
      .execute(_: HttpReads[O], _: ExecutionContext))
      .expects(*, *)
      .returning(Future.successful(response))

  def mockHttpClientV2ExecuteException[O: HttpReads](response: Throwable): CallHandler2[HttpReads[O], ExecutionContext, Future[O]] =
    (mockRequestBuilder
      .execute(_: HttpReads[O], _: ExecutionContext))
      .expects(*, *)
      .returning(Future.failed(response))

  def mockHttpClientV2Put(url: URL): CallHandler2[URL, HeaderCarrier, RequestBuilder] =
    (mockHttp
      .put(_: URL)(_: HeaderCarrier))
      .expects(url, *)
      .returning(mockRequestBuilder)

  def mockHttpClientV2WithBody(): Unit =
    (mockRequestBuilder
      .withBody(_: JsValue)(_: BodyWritable[JsValue], _: IzumiTag[JsValue], _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(mockRequestBuilder)

}
