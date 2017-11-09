/*
 * Copyright 2017 HM Revenue & Customs
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

import helpers.{AuthHelper, FakeRequestHelper}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import models.{KnownFact, KnownFactsForService}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.play.OneAppPerSuite

import scala.concurrent.Future
import uk.gov.hmrc.http.HttpResponse

class GovernmentGatewayAdminConnectorSpec extends UnitSpec with MockitoSugar with FakeRequestHelper with OneAppPerSuite with AuthHelper {

  val testConnector = new GovernmentGatewayAdminConnectorImpl(mockHttp, testAppConfig)

  def mockGatewayResponse(response: HttpResponse): Unit =
    when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(),Matchers.any())).
      thenReturn(Future.successful(response))

  "GovernmentGatewayAdminConnector" should {
    "Get the serviceUrl from the ggAdminURL in config" in {
      testConnector.serviceURL shouldBe testAppConfig.ggAdminURL
    }
  }

  "GovernmentGatewayAdminConnector" when {

    "called for successful set of known facts" should {
      lazy val result = testConnector.addKnownFacts(KnownFactsForService(List(
        KnownFact("HMRC-TAVC-ORG", "XXTAVC000123456"),
        KnownFact("postalCode", "AA1 1AA")
      )))
      lazy val response = await(result)

      "return status OK (200)" in {
        mockGatewayResponse(HttpResponse(OK))
        response.status shouldBe OK
      }
    }

    "called for unsuccessful set of known facts" should {

      val unsuccessfulSubscribeJson = Json.parse( """{ "Message": "An error occured" }""")
      lazy val result = testConnector.addKnownFacts(KnownFactsForService(List()))
      lazy val response = await(result)

      "return status BAD_REQUEST (400)" in {
        mockGatewayResponse(HttpResponse(BAD_REQUEST, responseJson = Some(unsuccessfulSubscribeJson)))
        response.status shouldBe BAD_REQUEST
      }

      "have a Json result with the returned error message" in {
        response.json shouldBe unsuccessfulSubscribeJson
      }
    }
  }
}
