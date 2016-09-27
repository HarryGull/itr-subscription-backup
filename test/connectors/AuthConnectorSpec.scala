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
import org.mockito.Matchers
import org.mockito.Mockito._

import scala.concurrent.Future

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

  val affinityResponse = (key: String) => Json.parse(
    s"""{"uri":"/auth/oid/57e915480f00000f006d915b","confidenceLevel":50,"credentialStrength":"weak",
      |"userDetailsLink":"http://localhost:9978/user-details/id/57e915482200005f00b0b55e","legacyOid":"57e915480f00000f006d915b",
      |"new-session":"/auth/oid/57e915480f00000f006d915b/session","ids":"/auth/oid/57e915480f00000f006d915b/ids",
      |"credentials":{"gatewayId":"872334723473244"},"accounts":{},"lastUpdated":"2016-09-26T12:32:08.734Z",
      |"loggedInAt":"2016-09-26T12:32:08.734Z","levelOfAssurance":"1","enrolments":"/auth/oid/57e915480f00000f006d915b/enrolments",
      |"affinityGroup":"$key","correlationId":"9da194b9490024bae213f18d5b34fedf41f2c3236b434975333a7bdb0fe548ec","credId":"872334723473244"}""".stripMargin
  )

  "AuthConnector.getCurrentAuthority" should {
    "return Some(Authority) when auth info is found " in {
      mockGetAffinityGroupResponse(HttpResponse(OK, Some(affinityResponse("Authorised"))))
      mockGetCurrentAuthority(HttpResponse(OK, Some(authResponse)))
      await(TestAuthConnector.getCurrentAuthority()) shouldBe Some(Authority(uri, oid, userDetailsLink, ConfidenceLevel.L50))
    }

    "return None when no auth info is found" in {
      mockGetAffinityGroupResponse(HttpResponse(OK, Some(affinityResponse("Authorised"))))
      mockGetCurrentAuthority(HttpResponse(NOT_FOUND, None))
      await(TestAuthConnector.getCurrentAuthority()) shouldBe None
    }
  }

  "AuthConnector.getAffinityGroup" should {
    "return Some('Organisation') when a ORGANISATION is found" in {
      mockGetAffinityGroupResponse(HttpResponse(OK, Some(affinityResponse("Organisation"))))
      mockGetCurrentAuthority(HttpResponse(OK, Some(authResponse)))
      await(TestAuthConnector.getAffinityGroup(uri)) shouldBe Some("Organisation")
    }

    "return Some('Individual') when a Agent is found" in {
      mockGetAffinityGroupResponse(HttpResponse(OK, Some(affinityResponse("Agent"))))
      mockGetCurrentAuthority(HttpResponse(OK, Some(authResponse)))
      await(TestAuthConnector.getAffinityGroup(uri)) shouldBe Some("Agent")
    }

    "return None when a None is found" in {
      mockGetAffinityGroupResponse(HttpResponse(NOT_FOUND, None))
      mockGetCurrentAuthority(HttpResponse(OK, Some(authResponse)))
      await(TestAuthConnector.getAffinityGroup(uri)) shouldBe None
    }
  }

}
