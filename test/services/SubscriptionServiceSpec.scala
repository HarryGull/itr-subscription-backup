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

package services

import common.GetSubscriptionResponses
import connectors.{AuthenticatorConnector, GovernmentGatewayAdminConnector, GovernmentGatewayConnector, SubscriptionETMPConnector}
import helpers.FakeRequestHelper
import play.api.libs.json.Json
import helpers.TestHelper._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.SessionId
import play.api.test.Helpers._
import org.scalatestplus.play.OneAppPerSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionServiceSpec extends UnitSpec with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach with OneAppPerSuite {

  object TestSubscriptionService extends SubscriptionService {
    override val subscriptionETMPConnector: SubscriptionETMPConnector = mock[SubscriptionETMPConnector]
    override val ggAdminConnector = mock[GovernmentGatewayAdminConnector]
    override val ggConnector = mock[GovernmentGatewayConnector]
    override val authenticatorConnector = mock[AuthenticatorConnector]
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("testID")))

  def mockEtmpResponse(response: HttpResponse): Unit = when(TestSubscriptionService.subscriptionETMPConnector.subscribeToEtmp(Matchers.any(), Matchers.any())
  (Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))

  def mockGetSubscriptionEtmpResponse(response: HttpResponse): Unit =
    when(TestSubscriptionService.subscriptionETMPConnector.getSubscription(Matchers.any())
  (Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))

  def mockGgAdminResponse(response: HttpResponse): Unit = when(TestSubscriptionService.ggAdminConnector.addKnownFacts(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(response))

  def mockGgResponse(response: HttpResponse): Unit = when(TestSubscriptionService.ggConnector.addEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(response))

  def mockAuthenticatorResponse(response: HttpResponse): Unit =
    when(TestSubscriptionService.authenticatorConnector.refreshProfile(Matchers.any())).thenReturn(Future.successful(response))


  "SubscriptionService" should {
    "use the correct ETMP connector" in {
      SubscriptionService.subscriptionETMPConnector shouldBe SubscriptionETMPConnector
    }

    "use the correct ggAdmin connector" in {
      SubscriptionService.ggAdminConnector shouldBe GovernmentGatewayAdminConnector
    }

    "use the correct gg connector" in {
      SubscriptionService.ggConnector shouldBe GovernmentGatewayConnector
    }

    "use the correct authenticator connector" in {
      SubscriptionService.authenticatorConnector shouldBe AuthenticatorConnector
    }
  }

  "Calling SubscribeService.subscribe" when {

    "returns successful ETMP Subscription, GG Admin, GG Enrol and Authenticator responses" should {

      "return an NO_CONTENT response (204)" in {
        mockEtmpResponse(HttpResponse(OK, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(OK))
        mockGgResponse(HttpResponse(OK))
        mockAuthenticatorResponse(HttpResponse(NO_CONTENT))
        val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
        await(result).status shouldBe NO_CONTENT
      }
    }

    "returns a NON-Successful ETMP Subscription" should {

      lazy val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
      lazy val response = await(result)

      "return an BAD_REQUEST response (400)" in {
        mockEtmpResponse(HttpResponse(BAD_REQUEST, Some(etmpFailureResponse)))
        response.status shouldBe BAD_REQUEST
      }

      "return Json error message" in {
        mockEtmpResponse(HttpResponse(BAD_REQUEST, Some(etmpFailureResponse)))
        response.json shouldBe etmpFailureResponse
      }
    }

    "returns a successful ETMP Subscription and NON-successful GG Admin response" should {

      lazy val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
      lazy val response = await(result)

      "return an BAD_REQUEST response (400)" in {
        mockEtmpResponse(HttpResponse(OK, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(BAD_REQUEST, responseJson = Some(ggAdminFailureResponse)))
        response.status shouldBe BAD_REQUEST
      }

      "return Json error message" in {
        mockEtmpResponse(HttpResponse(OK, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(BAD_REQUEST, responseJson = Some(ggAdminFailureResponse)))
        response.json shouldBe ggAdminFailureResponse
      }
    }

    "returns successful ETMP Subscription and GG Admin responses and a Non-Successful GG Enrol response" should {

      lazy val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
      lazy val response = await(result)

      "return an BAD_REQUEST response (400)" in {
        mockEtmpResponse(HttpResponse(OK, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(OK))
        mockGgResponse(HttpResponse(BAD_REQUEST, Some(ggEnrolFailureResponse)))
        response.status shouldBe BAD_REQUEST
      }

      "return Json error message" in {
        mockEtmpResponse(HttpResponse(OK, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(OK))
        mockGgResponse(HttpResponse(BAD_REQUEST, Some(ggEnrolFailureResponse)))
        response.json shouldBe ggEnrolFailureResponse
      }
    }

    "returns successful ETMP Subscription, GG Admin and GG Enrol response but Non-Successful Authenticator response" should {

      lazy val result = TestSubscriptionService.subscribe(dummyValidSafeID, dummySubscriptionRequestValid, dummyValidPostcode)
      lazy val response = await(result)

      "return an BAD_REQUEST response (400)" in {
        mockEtmpResponse(HttpResponse(OK, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(OK))
        mockGgResponse(HttpResponse(OK))
        mockAuthenticatorResponse(HttpResponse(BAD_REQUEST, responseJson = Some(authenticatorFailureResponse)))
        response.status shouldBe BAD_REQUEST
      }

      "return Json error message" in {
        mockEtmpResponse(HttpResponse(OK, Some(etmpSuccessResponse)))
        mockGgAdminResponse(HttpResponse(OK))
        mockGgResponse(HttpResponse(OK))
        mockAuthenticatorResponse(HttpResponse(BAD_REQUEST, responseJson = Some(authenticatorFailureResponse)))
        response.json shouldBe authenticatorFailureResponse
      }
    }
  }

  "Calling SubscribeService.knownFactsBuilder" when {

    "given an OK response from ETMP which includes the tavcRegNumber" should {
      lazy val result = TestSubscriptionService.knownFactsBuilder(dummyValidTavcRegNumber, dummyValidPostcode)
      lazy val response = await(result)

      val expectedJson = Json.parse(
        s"""
          |{
          |  "facts":[
          |     {"type":"TAVCRef","value":"$dummyValidTavcRegNumber"},
          |     {"type":"Postcode","value":"$dummyValidPostcode"}
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

    "given a OK response from ETMP which includes the tavcRegNumber" should {
      lazy val result = TestSubscriptionService.addKnownFacts(HttpResponse(OK, Some(etmpSuccessResponse)), dummyValidPostcode)
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

  "Calling SubscribeService.enrolmentRequestBuilder" when {

    "provided with the tavcReference and postCode" should {
      lazy val result = TestSubscriptionService.enrolmentRequestBuilder(dummyValidTavcRegNumber, dummyValidPostcode)
      lazy val response = await(result)

      val expectedJson = Json.parse(
        s"""
           | {
           |    "portalId":"$portalId",
           |    "serviceName":"$serviceName",
           |    "friendlyName":"$friendlyServiceName",
           |    "knownFacts": [ "$dummyValidTavcRegNumber", "$dummyValidPostcode" ]
           | }
        """.stripMargin
      )

      "Generate a Json object in the correct format to be posted to GG-Admin" in {
        Json.toJson(response) shouldBe expectedJson
      }
    }
  }

  "Calling SubscribeService.addEnrolment" when {

    "given an OK response from GG Admin" should {
      lazy val result = TestSubscriptionService.addEnrolment(
        ggAdminResponse = HttpResponse(OK),
        etmpResponse = HttpResponse(OK, responseJson = Some(etmpSuccessResponse)),
        postCode = dummyValidPostcode
      )
      lazy val response = await(result)

      "return an OK response (200)" in {
        when(TestSubscriptionService.ggConnector.addEnrolment(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
        response.status shouldBe OK
      }
    }

    "given a response other than OK from GG Admin" should {
      lazy val result = TestSubscriptionService.addEnrolment(
        ggAdminResponse = HttpResponse(BAD_REQUEST, responseJson = Some(ggAdminFailureResponse)),
        etmpResponse = HttpResponse(OK, responseJson = Some(etmpSuccessResponse)),
        postCode = dummyValidPostcode
      )
      lazy val response = await(result)

      "return a BAD_REQUEST response (400)" in {
        response.status shouldBe BAD_REQUEST
      }

      "return an error message json response" in {
        response.json shouldBe ggAdminFailureResponse
      }
    }
  }

  "Calling SubscribeService.refreshAuthProfile" should {
    "return the response propagated" when {
      "Return status NO_CONTENT (204)" in {
        mockAuthenticatorResponse(HttpResponse(NO_CONTENT))
        val result = TestSubscriptionService.refreshAuthProfile(
          ggResponse = HttpResponse(OK)
        )
        await(result).status shouldBe NO_CONTENT
      }

      "Return status BAD_REQUEST (400)" in {
        mockAuthenticatorResponse(HttpResponse(BAD_REQUEST))
        val result = TestSubscriptionService.refreshAuthProfile(
          ggResponse = HttpResponse(OK)
        )
        await(result).status shouldBe BAD_REQUEST
      }
    }
  }

  "Calling SubscribeService.getSubscription" when {
    "returns successful full ETMP response" should {
      lazy val result = TestSubscriptionService.getSubscription(dummyValidTavcRegNumber)
      lazy val response = await(result)

      "return a OK response (200)" in {
        mockGetSubscriptionEtmpResponse(HttpResponse(OK, Some(GetSubscriptionResponses.getSubFull)))
        response.status shouldBe OK
      }

      "return the expected json response containing the returned subscription" in {
        response.json shouldBe GetSubscriptionResponses.getSubFull
      }
    }
  }

  "Calling SubscribeService.getSubscription" when {
    "returns successful no address ETMP response" should {
      lazy val result = TestSubscriptionService.getSubscription(dummyValidTavcRegNumber)
      lazy val response = await(result)

      "return a OK response (200)" in {
        mockGetSubscriptionEtmpResponse(HttpResponse(OK, Some(GetSubscriptionResponses.getSubNoAddress)))
        response.status shouldBe OK
      }
      "return the expected json response containing the returned subscription" in {
        mockGetSubscriptionEtmpResponse(HttpResponse(OK, Some(GetSubscriptionResponses.getSubNoAddress)))
        response.json shouldBe GetSubscriptionResponses.getSubNoAddress
      }
    }
  }

  "Calling SubscribeService.getSubscription" when {
    "return a Bad request response" should {
      lazy val result = TestSubscriptionService.getSubscription(dummyValidTavcRegNumber)
      lazy val response = await(result)
      "return a BAD_REQUEST response (400)" in {
        mockGetSubscriptionEtmpResponse(HttpResponse(BAD_REQUEST))
        response.status shouldBe BAD_REQUEST
      }
    }
  }

}
