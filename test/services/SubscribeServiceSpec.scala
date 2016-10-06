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
import connectors.AuthenticatorConnector
import helpers.FakeRequestHelper
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class SubscribeServiceSpec extends FakeApplication with UnitSpec with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  object TestSubscribeServiceSpec extends SubscribeService {
    override val authenticatorConnector = mock[AuthenticatorConnector]
  }

  "Calling SubscribeService.refreshAuthProfile" should {
    "return the response propagated" when {
      "Return status NO_CONTENT (204)" in {
        when(TestSubscribeServiceSpec.refreshAuthProfile(Matchers.any())).thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
        val result = TestSubscribeServiceSpec.refreshAuthProfile
        await(result).status shouldBe NO_CONTENT
      }

      "Return status BAD_REQUEST (400)" in {
        when(TestSubscribeServiceSpec.refreshAuthProfile(Matchers.any())).thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))
        val result = TestSubscribeServiceSpec.refreshAuthProfile
        await(result).status shouldBe BAD_REQUEST
      }
    }
  }
}