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
import models.ggEnrolment.{EnrolRequestModel, EnrolResponseModel, IdentifierModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class GovernmentGatewayConnectorSpec extends UnitSpec with MockitoSugar with FakeRequestHelper with OneAppPerSuite with AuthHelper {

  val testConnector = new GovernmentGatewayConnectorImpl(mockHttp, testAppConfig)

  val enrolmentRequest = EnrolRequestModel(
    "Default",
    "HMRC-TAVC-ORG",
    "Tax Advantaged Venture Capital Schemes Enrolment",
    Seq(
      "XXTAVC000123456",
      "AA1 1AA"
    )
  )

  val successResponse = Json.toJson(EnrolResponseModel(
    "HMRC-TAVC-ORG",
    "Activated",
    List(
      IdentifierModel("tavcRegNumber","XXTAVC000123456"),
      IdentifierModel("postalCode","AA1 1AA")
    )
  ))

  val errorResponse = Json.parse( """{ "Message": "An error occured" }""")

  def mockGatewayResponse(response: HttpResponse): Unit =
    when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(response))

  "GovernmentGatewayConnector" should {
    "Get the serviceUrl from the ggURL in config" in {
      testConnector.serviceURL shouldBe testAppConfig.ggURL
    }
  }

  "GovernmentGatewayConnector" when {

    "called and a successful response is expected" should {
      lazy val result = testConnector.addEnrolment(enrolmentRequest)
      lazy val response = await(result)

      "return status OK (200)" in {
        mockGatewayResponse(HttpResponse(OK, responseJson = Some(successResponse)))
        response.status shouldBe OK
      }

      "have a Json result with the expected enrolmentResponse" in {
        response.json shouldBe successResponse
      }
    }

    "called and an unsuccessful response is expected" should {
      lazy val result = testConnector.addEnrolment(enrolmentRequest)
      lazy val response = await(result)

      "return status BAD_REQUEST (400)" in {
        mockGatewayResponse(HttpResponse(BAD_REQUEST, responseJson = Some(errorResponse)))
        response.status shouldBe BAD_REQUEST
      }

      "have a Json result with the returned error message" in {
        response.json shouldBe errorResponse
      }
    }
  }
}
