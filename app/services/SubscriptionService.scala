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

import common.GovernmentGatewayConstants
import common.GovernmentGatewayConstants._
import connectors.{AuthenticatorConnector, GovernmentGatewayAdminConnector, GovernmentGatewayConnector, SubscriptionETMPConnector}
import model.SubscriptionRequest
import models.ggEnrolment.EnrolRequestModel
import models.{KnownFact, KnownFactsForService}
import play.api.http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

object SubscriptionService extends SubscriptionService{
  val subscriptionETMPConnector: SubscriptionETMPConnector = SubscriptionETMPConnector
  val ggAdminConnector: GovernmentGatewayAdminConnector = GovernmentGatewayAdminConnector
  val ggConnector: GovernmentGatewayConnector = GovernmentGatewayConnector
  val authenticatorConnector: AuthenticatorConnector = AuthenticatorConnector
}

trait SubscriptionService {

  val subscriptionETMPConnector: SubscriptionETMPConnector
  val ggAdminConnector: GovernmentGatewayAdminConnector
  val ggConnector: GovernmentGatewayConnector
  val authenticatorConnector: AuthenticatorConnector

  def subscribe(safeId: String,
                subscriptionRequest: SubscriptionRequest,
                postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    for {
      etmpResponse <- subscriptionETMPConnector.subscribeToEtmp(safeId,subscriptionRequest)
      ggAdminResponse <- addKnownFacts(etmpResponse, postcode)
      ggResponse <- addEnrolment(ggAdminResponse, etmpResponse, postcode)
      refreshAuthProfileResponse <- refreshAuthProfile(ggResponse)
    } yield refreshAuthProfileResponse

  def addKnownFacts(etmpResponse: HttpResponse, postCode: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    etmpResponse.status match {
      case CREATED => ggAdminConnector.addKnownFacts(knownFactsBuilder((etmpResponse.json \ etmpReferenceKey).as[String], postCode))
      case _ => Future.successful(etmpResponse)
    }

  def addEnrolment(ggAdminResponse: HttpResponse, etmpResponse: HttpResponse, postCode: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    ggAdminResponse.status match {
      case OK => ggConnector.addEnrolment(enrolmentRequestBuilder((etmpResponse.json \ etmpReferenceKey).as[String], postCode))
      case _ => Future.successful(ggAdminResponse)
    }

  def refreshAuthProfile(ggResponse: HttpResponse)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    ggResponse.status match {
      case OK => authenticatorConnector.refreshProfile
      case _ => Future.successful(ggResponse)
    }

  def knownFactsBuilder(tavReference: String, postCode: String): KnownFactsForService = {
    val knownFact1 = KnownFact(tavcReferenceKey, tavReference)
    val knownFact2 = KnownFact(tavcPostcodeKey, postCode)
    KnownFactsForService(List(knownFact1, knownFact2))
  }

  def enrolmentRequestBuilder(tavcReference: String, postCode: String): EnrolRequestModel =
    EnrolRequestModel(
      GovernmentGatewayConstants.tavcPortalIdentifier,
      GovernmentGatewayConstants.tavcServiceNameKey,
      GovernmentGatewayConstants.tavcFriendlyName,
      List(tavcReference, postCode)
    )
}
