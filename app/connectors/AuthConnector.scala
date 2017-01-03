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

import config.{MicroserviceAppConfig, WSHttp}
import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import auth.Authority
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AuthConnector extends ServicesConfig {

  def serviceUrl: String
  def authorityUri: String
  def http: HttpGet with HttpPost

  def getCurrentAuthority()(implicit hc: HeaderCarrier): Future[Option[Authority]] = {
    val getUrl = s"""$serviceUrl/$authorityUri"""
    http.GET[HttpResponse](getUrl).map {
      response => response.status match {
        case OK => {
          val uri = (response.json \ "uri").as[String]
          val oid = uri.substring(uri.lastIndexOf("/") + 1)
          val userDetails = (response.json \ "userDetailsLink").as[String]
          val confidenceLevel = (response.json \ "confidenceLevel").as[ConfidenceLevel]
          Some(Authority(uri, oid, userDetails, confidenceLevel))
        }
        case _ => None
      }
    }
  }

  def getAffinityGroup(url: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val getUrl = s"""$serviceUrl$url"""
    http.GET[HttpResponse](getUrl).map {
      response => response.status match {
        case OK => {
          val affinityGroup = (response.json \ "affinityGroup").as[String]
          Some(affinityGroup)
        }
        case _ => None
      }
    }
  }
}

object AuthConnector extends AuthConnector {
  lazy val serviceUrl = MicroserviceAppConfig.authURL
  val authorityUri = "auth/authority"
  val http: HttpGet with HttpPost = WSHttp
}
