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

import connectors.SubscriptionETMPConnector
import helpers.Constants._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.logging.SessionId
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class SubscriptionServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val sessionId = UUID.randomUUID.toString
  val mockHttp : WSHttp = mock[WSHttp]
  val mockSubscriptionETMPConnector : SubscriptionETMPConnector = mock[SubscriptionETMPConnector]

  class Setup {
    object TestSubscriptionService extends SubscriptionService {
      val subscriptionETMPConnector: SubscriptionETMPConnector = mockSubscriptionETMPConnector
    }
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  "The submission service" should {
    "use the correct connector" in new Setup {
      SubscriptionService.subscriptionETMPConnector shouldBe SubscriptionETMPConnector
    }
  }

  "The submission service should" should {
    "return a valid response" in new Setup {
      when(mockSubscriptionETMPConnector.subscribeToEtmp(Matchers.any(), Matchers.any())
      (Matchers.any(),Matchers.any())).thenReturn(Future.successful(HttpResponse(CREATED)))
      val result = TestSubscriptionService.subscribe(dummyValidSafeID,dummySubscriptionRequestValid)
      await(result).status shouldBe CREATED
    }
  }

}