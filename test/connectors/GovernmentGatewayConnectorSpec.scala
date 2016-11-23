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

import helpers.FakeRequestHelper
import helpers.GovernmentGatewayHelper._
import models.ggEnrolment.{EnrolRequestModel, EnrolResponseModel, IdentifierModel}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class GovernmentGatewayConnectorSpec extends UnitSpec with MockitoSugar with FakeRequestHelper with WithFakeApplication {

  object TestGGConnector extends GovernmentGatewayConnector {
    override val serviceURL = "government-gateway"
    override val enrolURI = "enrol"
    override val http: HttpGet with HttpPost = mockWSHttp
  }

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


  "GovernmentGatewayConnector" when {

    "called and a successful response is expected" should {
      lazy val result = TestGGConnector.addEnrolment(enrolmentRequest)
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
      lazy val result = TestGGConnector.addEnrolment(enrolmentRequest)
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
