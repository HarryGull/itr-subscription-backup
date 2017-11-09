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

package helpers

import auth.Authority
import config.TestAppConfig
import connectors.AuthConnector
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }

trait AuthHelper extends MockitoSugar {

  val oid = "foo"
  val uri = s"""/x/y/$oid"""
  val authURI = "/auth/authority"
  val userDetailsLink = "bar"
  val mockAuthConnector = mock[AuthConnector]

  val mockHttp = mock[WSHttp]
  val testAppConfig = new TestAppConfig

  implicit val hc = HeaderCarrier()

  def authorityBuilder(confidenceLevel: ConfidenceLevel): Option[Authority] = Some(Authority(uri, oid, userDetailsLink, confidenceLevel))

  def setUp(authority: Option[Authority], affinityGroup: Option[String]): Unit = {
    when(mockAuthConnector.getCurrentAuthority()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(authority))
    when(mockAuthConnector.getAffinityGroup(Matchers.anyString)(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(affinityGroup))
  }

  def mockGetCurrentAuthority(response: HttpResponse): Unit =
    when(mockHttp.GET[HttpResponse](Matchers.eq(s"${testAppConfig.authURL}$authURI"))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(response))

  def mockGetAffinityGroupResponse(response: HttpResponse): Unit =
    when(mockHttp.GET[HttpResponse](Matchers.eq(s"${testAppConfig.authURL}$uri"))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))

  object Authorities {
    val userCL0 = authorityBuilder(ConfidenceLevel.L0)
    val userCL50 = authorityBuilder(ConfidenceLevel.L50)
    val userCL100 = authorityBuilder(ConfidenceLevel.L100)
    val userCL200 = authorityBuilder(ConfidenceLevel.L200)
    val userCL300 = authorityBuilder(ConfidenceLevel.L300)
    val userCL500 = authorityBuilder(ConfidenceLevel.L500)
  }

  object AffinityGroups {
    val organisation = Some("Organisation")
    val agent = Some("Agent")
  }
}
