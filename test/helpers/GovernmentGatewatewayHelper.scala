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

package helpers

import models.{KnownFact, KnownFactsForService}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.ws.{WSGet, WSPost}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import scala.concurrent.Future

object GovernmentGatewayHelper extends MockitoSugar {

  class MockHttp extends WSGet with WSPost {
    override val hooks = NoneRequired
  }
  val mockWSHttp = mock[MockHttp]
  implicit val hc = HeaderCarrier()

  def knownFactsBuilder: List[(String, String)] => KnownFactsForService = keyValuePairs => {
    val knownFacts = for (keyValuePair <- keyValuePairs) yield KnownFact(keyValuePair._1, keyValuePair._2)
    KnownFactsForService(knownFacts)
  }

  def mockGatewayResponse(response: HttpResponse): Unit =
    when(mockWSHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(response))
}
