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

import config.WSHttp
import helpers.TestHelper._
import uk.gov.hmrc.play.test.WithFakeApplication
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import java.util.UUID

import common.GetSubscriptionResponses
import play.api.test.Helpers._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionETMPConnectorSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val mockHttp : WSHttp = mock[WSHttp]
  val sessionId = UUID.randomUUID.toString

  class Setup {
    object TestConnector extends SubscriptionETMPConnector {
      val serviceUrl = "dummy"
      val http = mockHttp
    }
  }

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")

  "SubscriptionETMPConnector" should {
    "use the correct http object" in {
      SubscriptionETMPConnector.http shouldBe WSHttp
    }
  }

  "calling subscribeToEtmp with a valid ackref and safeId" should {
    "return a valid response" in new Setup {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.anyString(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(HttpResponse(OK))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID,dummySubscriptionRequestValid)
      await(result).status shouldBe OK
    }
  }

  "Calling subscribeToEtmp with a invalid safeId" should {
    "return a BAD_REQUEST" in new Setup {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val result = TestConnector.subscribeToEtmp(dummyInvalidSafeID,dummySubscriptionRequestValid)
      await(result).status shouldBe OK
    }
  }

  "Calling subscribeToEtmp with a ackRef containing 'duplicate'" should {
    "return a BAD_REQUEST error" in new Setup{
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID,dummySubscriptionRequestDuplicate)
      await(result).status shouldBe BAD_REQUEST
    }
  }

  "Calling subscribeToEtmp with a ackRef containing 'notfound'" should {
    "return a NOT_FOUND Error" in new Setup {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND)))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID,dummySubscriptionRequestNotFound)
      await(result).status shouldBe NOT_FOUND
    }
  }

  "Calling subscribeToEtmp with a ackRef containing 'serviceunavailable'" should {
    "return a SERVICE UNAVAILABLE ERROR" in new Setup  {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE)))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID, dummySubscriptionRequestServiceUnavailable)
      await(result).status shouldBe SERVICE_UNAVAILABLE
    }
  }

  "Calling subscribeToEtmp with a ackRef containing 'servererror'" should {
    "return a INTERNAL SERVER ERROR" in new Setup  {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID,dummySubscriptionRequestServerError)
      await(result).status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling subscribeToEtmp with a ackRef containing 'missingregime'" should {
    "return a INTERNAL SERVER ERROR" in new Setup  {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID,dummySubscriptionRequestMissingRegime)
      await(result).status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling subscribeToEtmp with a ackRef containing 'sapnumbermissing'" should {
    "return a INTERNAL SERVER ERROR" in new Setup  {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID,dummySubscriptionRequestSapNumberMissing)
      await(result).status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling subscribeToEtmp with a ackRef containing 'notprocessed'" should {
    "return a SERVICE UNAVAILABLE ERROR" in new Setup  {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE)))
      val result = TestConnector.subscribeToEtmp(dummyValidSafeID, dummySubscriptionRequestNotProcessed)
      await(result).status shouldBe SERVICE_UNAVAILABLE
    }
  }

  "Calling subscribeToEtmp with a valid Tavc Ref'" should {
    "return a NOT_FOUND Error if a NOT_FOUND found returned from DES" in new Setup {
      when(mockHttp.GET[HttpResponse](Matchers.eq(s"${TestConnector.serviceUrl}/tax-assured-venture-capital/taxpayers/$dummyValidTavcRegNumber/subscription"))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND)))
      val result = TestConnector.getSubscription(dummyValidTavcRegNumber)
      await(result).status shouldBe NOT_FOUND
    }
  }

  "Calling subscribeToEtmp with a valid Tavc Ref'" should {
    "return an OK with expected JSON body if a matching records returned from DES" in new Setup {
      when(mockHttp.GET[HttpResponse](Matchers.eq(s"${TestConnector.serviceUrl}/tax-assured-venture-capital/taxpayers/$dummyValidTavcRegNumber/subscription"))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(GetSubscriptionResponses.getSubFull))))
      val result = TestConnector.getSubscription(dummyValidTavcRegNumber)
      val response = await(result)
      response.status shouldBe OK
      response.json shouldBe GetSubscriptionResponses.getSubFull

    }
  }
}