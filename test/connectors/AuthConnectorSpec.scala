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

import auth.{Authority, Enrolment, Identifier}
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AuthConnectorSpec extends FakeApplication with UnitSpec with MockitoSugar {

  object TestConnector extends AuthConnector {
    override def serviceUrl: String = "localhost"
    override def authorityUri: String = "auth/authority"
    override def http: HttpGet with HttpPost = mockHttp
  }

  implicit val hc = HeaderCarrier()
  val mockHttp = mock[HttpGet with HttpPost]
  val oid = "foo"
  val uri = s"""/x/y/$oid"""
  val userDetailsLink = "bar"
  val confidenceLevel = ConfidenceLevel.L50
  val tavcRef = "AA1234567890000"
  val postcode = "ACB123"
  val authResponse = Json.parse(s"""{"uri":"$uri","userDetailsLink":"$userDetailsLink","confidenceLevel":$confidenceLevel}""")
  val enrolmentResponse = (key: String) => Json.parse(
    """[{"key":"IR-SA","identifiers":[{"key":"UTR","value":"12345"}],"state":"Activated"},""" +
  s"""{"key":"$key","identifiers":[{"key":"TAVCRef","value":"$tavcRef"},{"key":"Postcode","value":"$postcode"}],"state":"Activated"}]"""
  )

  "AuthConnector.getCurrentAuthority" should {
    "return Some(Authority) when auth info is found" in {
      when(mockHttp.GET[HttpResponse](Matchers.eq("localhost/auth/authority"))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK,Some(authResponse))))
      val result = await(TestConnector.getCurrentAuthority())
      result shouldBe Some(Authority(uri,oid,userDetailsLink,confidenceLevel))
    }
    "return None when no auth info is found" in {
      when(mockHttp.GET[HttpResponse](Matchers.eq("localhost/auth/authority"))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND,None)))
      val result = await(TestConnector.getCurrentAuthority())
      result shouldBe None
    }
  }

  "AuthConnector.getTAVCEnrolment" should {
    "return Some(Enrolment) when a TAVC enrolment is found" in {
      when(mockHttp.GET[HttpResponse](Matchers.eq(s"localhost$uri/enrolments"))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK,Some(enrolmentResponse("HMRC-TAVC-ORG")))))
      val result = await(TestConnector.getTAVCEnrolment(uri))
      result shouldBe Some(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TAVCRef",tavcRef),Identifier("Postcode",postcode)),"Activated"))
    }
    "return None when no TAVC enrolment is found" in {
      when(mockHttp.GET[HttpResponse](Matchers.eq(s"localhost$uri/enrolments"))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK,Some(enrolmentResponse("HMCE-VATDEC-ORG")))))
      val result = await(TestConnector.getTAVCEnrolment(uri))
      result shouldBe None
    }
  }

}
