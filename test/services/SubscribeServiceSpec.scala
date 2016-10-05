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

package services

import java.util.UUID

import connectors.GovernmentGatewayAdminConnector
import helpers.FakeRequestHelper
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SubscribeServiceSpec extends FakeApplication with UnitSpec with MockitoSugar with FakeRequestHelper  {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestSubscribeServiceSpec extends SubscribeService {
    override val ggAdminConnector = mock[GovernmentGatewayAdminConnector]
  }

  val postCode = "TF3 4ER"

  val etmpSuccessResponse = Json.parse(
    """
      |{
      |  "processingDate": "2016-10-05T09:30:47Z",
      |  "tavcRegNumber": "XXTAVC000123456"
      |}
    """.stripMargin
  )

  val etmpFailureResponse = Json.parse(
    """
      |{
      |  "Message": "An Error Message"
      |}
    """.stripMargin
  )

  "Calling SubscribeService.knownFactsBuilder" when {

    "given an OK response from ETMP which includes the tavcRegNumber" should {
      lazy val result = TestSubscribeServiceSpec.knownFactsBuilder(HttpResponse(OK, Some(etmpSuccessResponse)), postCode)
      lazy val response = await(result)

      val expectedJson = Json.parse(
        """
          |{
          |  "facts":[
          |     {"type":"tavcRegNumber","value":"XXTAVC000123456"},
          |     {"type":"postalCode","value":"TF3 4ER"}
          |  ]
          |}
        """.stripMargin
      )

      "Generate a Json object in the correct format to be posted to GG-Admin" in {
        Json.toJson(response) shouldBe expectedJson
      }
    }
  }

  "Calling SubscribeService.addKnownFacts" when {

    "given an OK response from ETMP which includes the tavcRegNumber" should {
      lazy val result = TestSubscribeServiceSpec.addKnownFacts(HttpResponse(OK, Some(etmpSuccessResponse)), postCode)
      lazy val response = await(result)

      "return an OK response (200)" in {
        when(TestSubscribeServiceSpec.ggAdminConnector.addKnownFacts(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
        response.status shouldBe OK
      }
    }

    "given a response other than OK from ETMP" should {
      lazy val result = TestSubscribeServiceSpec.addKnownFacts(HttpResponse(BAD_REQUEST, Some(etmpFailureResponse)), postCode)
      lazy val response = await(result)

      "return a BAD_REQUEST response (400)" in {
        response.status shouldBe BAD_REQUEST
      }

      "return an error message json response" in {
        response.json shouldBe etmpFailureResponse
      }
    }
  }
}

