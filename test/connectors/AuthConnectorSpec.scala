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

package connectors

import auth.Authority
import config.WSHttp
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import helpers.AuthHelper._
import uk.gov.hmrc.play.config.ServicesConfig

class AuthConnectorSpec extends UnitSpec with MockitoSugar with WithFakeApplication with ServicesConfig {

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
    s"""{"uri":"/auth/oid/00000000000000000000000","confidenceLevel":50,"credentialStrength":"weak",
      |"userDetailsLink":"http://localhost:9978/user-details/id/000000000000000000000000","legacyOid":"00000000000000000000000",
      |"new-session":"/auth/oid/00000000000000000000000/session","ids":"/auth/oid/00000000000000000000000/ids",
      |"credentials":{"gatewayId":"000000000000000"},"accounts":{},"lastUpdated":"2016-09-26T12:32:08.734Z",
      |"loggedInAt":"2016-09-26T12:32:08.734Z","levelOfAssurance":"1","enrolments":"/auth/oid/00000000000000000000000/enrolments",
      |"affinityGroup":"$key","correlationId":"0000000000000000000000000000000000000000000000000000000000000000","credId":"000000000000000"}""".stripMargin
  )

  "AuthConnector" should {
    "Use WSHttp" in {
      AuthConnector.http shouldBe WSHttp
    }
    "Get the serviceUrl from the base url of auth in config" in {
      AuthConnector.serviceUrl shouldBe baseUrl("auth")
    }
  }

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
