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

import auth.Authority
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import helpers.AuthHelper._

class AuthConnectorSpec extends FakeApplication with UnitSpec with MockitoSugar {

  object TestAuthConnector extends AuthConnector {
    override def serviceUrl: String = "localhost"
    override def authorityUri: String = "auth/authority"
    override def http: HttpGet with HttpPost = mockHttp
  }

  val authResponse = Json.parse(
    s"""{
       |  "uri":"$uri",
       |  "userDetailsLink":"$userDetailsLink",
       |  "confidenceLevel":${ConfidenceLevel.L50}}""".stripMargin
  )

  "AuthConnector.getCurrentAuthority" should {
    "return Some(Authority) when auth info is found" in {
      mockGetCurrentAuthority(HttpResponse(OK, Some(authResponse)))
      await(TestAuthConnector.getCurrentAuthority()) shouldBe Some(Authority(uri, oid, userDetailsLink, ConfidenceLevel.L50))
    }

    "return None when no auth info is found" in {
      mockGetCurrentAuthority(HttpResponse(NOT_FOUND, None))
      await(TestAuthConnector.getCurrentAuthority()) shouldBe None
    }
  }

}
