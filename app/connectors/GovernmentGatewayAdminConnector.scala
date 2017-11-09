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

import config.{AppConfig, WSHttp}
import models._
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import common.GovernmentGatewayConstants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.google.inject._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

@Singleton
class GovernmentGatewayAdminConnectorImpl @Inject()(http: WSHttp, applicationConfig: AppConfig)
  extends GovernmentGatewayAdminConnector with ServicesConfig with RawResponseReads {

  lazy val serviceURL = applicationConfig.ggAdminURL
  val addKnownFactsURI = "known-facts"

  def addKnownFacts(knownFacts: KnownFactsForService)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val jsonData = Json.toJson(knownFacts)
    val baseUrl = s"""$serviceURL/government-gateway-admin/service"""
    val postUrl = s"""$baseUrl/${GovernmentGatewayConstants.tavcServiceNameKey}/$addKnownFactsURI"""

    http.POST[JsValue, HttpResponse](postUrl, jsonData) map {
      response =>
        response.status match {
          case OK => response
          case status =>
            Logger.warn(s"[GovernmentGatewayAdminConnector][addKnownFacts] - status: $status Error ${response.body}")
            response
        }
    }
  }
}

trait GovernmentGatewayAdminConnector {
  def addKnownFacts(knownFacts: KnownFactsForService)(implicit hc: HeaderCarrier): Future[HttpResponse]
}
