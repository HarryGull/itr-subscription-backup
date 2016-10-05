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
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeApplication
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import helpers.GovernmentGatewayHelper._
import models.{KnownFact, KnownFactsForService}

class GovernmentGatewayAdminConnectorSpec extends FakeApplication with UnitSpec with MockitoSugar with FakeRequestHelper {

  object TestGGAdminConnector extends GovernmentGatewayAdminConnector {
    override val serviceURL = "government-gateway-admin"
    override val addKnownFactsURI = "known-facts"
    override val http: HttpGet with HttpPost = mockWSHttp
  }

  "GovernmentGatewayAdminConnector" when {

    "called for successful set of known facts" should {
      lazy val result = TestGGAdminConnector.addKnownFacts(KnownFactsForService(List(
        KnownFact("HMRC-TAVC-ORG", "XXTAVC000123456"),
        KnownFact("postalCode", "TF3 4ER")
      )))

      "return status OK (200)" in {
        mockGatewayResponse(HttpResponse(OK))
        await(result).status shouldBe OK
      }
    }

    "called for unsuccessful set of known facts" should {

      val unsuccessfulSubscribeJson = Json.parse( """{ "Message": "An error occured" }""")
      lazy val result = TestGGAdminConnector.addKnownFacts(KnownFactsForService(List()))

      "return status BAD_REQUEST (400)" in {
        mockGatewayResponse(HttpResponse(BAD_REQUEST, responseJson = Some(unsuccessfulSubscribeJson)))
        await(result).status shouldBe BAD_REQUEST
      }

      "have a Json result with the returned error message" in {
        await(result).json shouldBe unsuccessfulSubscribeJson
      }
    }
  }
}
