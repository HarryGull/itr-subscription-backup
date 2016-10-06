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

import connectors.GovernmentGatewayAdminConnector
import helpers.FakeRequestHelper
import play.api.libs.json.Json
import connectors.SubscriptionETMPConnector
import helpers.Constants._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.SessionId
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionServiceSpec extends UnitSpec with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach with WithFakeApplication  {

  object TestSubscriptionService extends SubscriptionService {
    override val subscriptionETMPConnector: SubscriptionETMPConnector = mock[SubscriptionETMPConnector]
    override val ggAdminConnector = mock[GovernmentGatewayAdminConnector]
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("testID")))

  def mockEtmpResponse(response: HttpResponse): Unit = when(TestSubscriptionService.subscriptionETMPConnector.subscribeToEtmp(Matchers.any(), Matchers.any())
  (Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))

  def mockGgAdminResponse(response: HttpResponse): Unit = when(TestSubscriptionService.ggAdminConnector.addKnownFacts(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(response))

  "The submission service" should {
    "use the correct ETMP connector" in {
      SubscriptionService.subscriptionETMPConnector shouldBe SubscriptionETMPConnector
    }

    "use the correct ggAdmin connector" in {
      SubscriptionService.ggAdminConnector shouldBe GovernmentGatewayAdminConnector
    }
  }

  "Calling SubscribeService.knownFactsBuilder" when {

    "given an OK response from ETMP which includes the tavcRegNumber" should {
      lazy val result = TestSubscriptionService.knownFactsBuilder(HttpResponse(OK, Some(etmpSuccessResponse)), dummyValidPostcode)
      lazy val response = await(result)

      val expectedJson = Json.parse(
        s"""
          |{
          |  "facts":[
          |     {"type":"tavcRegNumber","value":"$dummyValidTavcRegNumber"},
          |     {"type":"postalCode","value":"$dummyValidPostcode"}
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

    "given a CREATED response from ETMP which includes the tavcRegNumber" should {
      lazy val result = TestSubscriptionService.addKnownFacts(HttpResponse(CREATED, Some(etmpSuccessResponse)), dummyValidPostcode)
      lazy val response = await(result)

      "return a OK response (200)" in {
        when(TestSubscriptionService.ggAdminConnector.addKnownFacts(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
        response.status shouldBe OK
      }
    }

    "given a response other than OK from ETMP" should {
      lazy val result = TestSubscriptionService.addKnownFacts(HttpResponse(BAD_REQUEST, Some(etmpFailureResponse)), dummyValidPostcode)
      lazy val response = await(result)

      "return a BAD_REQUEST response (400)" in {
        response.status shouldBe BAD_REQUEST
      }

      "return an error message json response" in {
        response.json shouldBe etmpFailureResponse
      }
    }
  }

  "Calling SubscribeService.subscribe" when {

    "returns a successful etmpResponse and successful GG Admin response" should {

      "return an OK response (200)" in {
        mockEtmpResponse(HttpResponse(CREATED, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(OK))
        val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
        await(result).status shouldBe OK
      }
    }

    "returns a NON-Successful etmpResponse" should {

      "return an BAD_REQUEST response (400)" in {
        mockEtmpResponse(HttpResponse(BAD_REQUEST, Some(etmpFailureResponse)))
        val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
        await(result).status shouldBe BAD_REQUEST
      }
    }

    "returns a successful etmpResponse and NON-successful GG Admin response" should {

      "return an OK response (200)" in {
        mockEtmpResponse(HttpResponse(CREATED, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(BAD_REQUEST))
        val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
        await(result).status shouldBe BAD_REQUEST
      }
    }
  }
}