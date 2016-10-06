/*
 * Copyright 2016 HM Revenue & Customs
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

package connectors

import org.scalatest.mock.MockitoSugar
import play.api.test.FakeApplication
import play.api.test.Helpers._
import org.mockito.Mockito._
import org.mockito.Matchers
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import helpers.AuthHelper._
import uk.gov.hmrc.play.http.ws.{WSGet, WSPost}

import scala.concurrent.Future

class AuthenticatorConnectorSpec extends FakeApplication with UnitSpec with MockitoSugar {

  class MockHttp extends WSGet with WSPost {
    override val hooks = NoneRequired
  }
  val mockWSHttp = mock[MockHttp]

  object TestAuthenticatorConnector extends AuthenticatorConnector {
    override val serviceURL = "localhost"
    override val refreshURI = "authenticator/refresh-profile"
    override val http = mockWSHttp
  }

  def setupMock(response: HttpResponse): Unit =
    when(TestAuthenticatorConnector.http.POSTEmpty[HttpResponse]
      (Matchers.eq(s"${TestAuthenticatorConnector.serviceURL}/${TestAuthenticatorConnector.refreshURI}"))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))

  "AuthenticatorConnector.refreshProfile" should {
    "return Status NO_CONTENT (204) when successful response received" in {
      setupMock(HttpResponse(NO_CONTENT))
      val result = TestAuthenticatorConnector.refreshProfile
      val response = await(result)
      response.status shouldBe NO_CONTENT
    }

    "propogate response when a status other than NO_CONTENT (204) is returned" in {
      setupMock(HttpResponse(BAD_REQUEST))
      val result = TestAuthenticatorConnector.refreshProfile
      val response = await(result)
      response.status shouldBe BAD_REQUEST
    }
  }
}
