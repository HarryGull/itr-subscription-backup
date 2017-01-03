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

import helpers.FakeRequestHelper
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeApplication
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import helpers.GovernmentGatewayHelper._
import models.{KnownFact, KnownFactsForService}

class GovernmentGatewayAdminConnectorSpec extends UnitSpec with MockitoSugar with FakeRequestHelper with WithFakeApplication {

  object TestGGAdminConnector extends GovernmentGatewayAdminConnector {
    override val serviceURL = "government-gateway-admin"
    override val addKnownFactsURI = "known-facts"
    override val http: HttpGet with HttpPost = mockWSHttp
  }

  "GovernmentGatewayAdminConnector" when {

    "called for successful set of known facts" should {
      lazy val result = TestGGAdminConnector.addKnownFacts(KnownFactsForService(List(
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
      lazy val result = TestGGAdminConnector.addKnownFacts(KnownFactsForService(List()))
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
