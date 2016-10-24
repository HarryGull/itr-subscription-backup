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

package controllers

import auth.{Authorisation, Authorised, NotAuthorised}
import play.api.libs.json.Json
import connectors.AuthConnector
import metrics.MetricsEnum
import model.{Error, SubscriptionRequest, SubscriptionType}
import services.{AuditService, SubscriptionService}
import uk.gov.hmrc.play.microservice.controller.BaseController
import play.api.mvc._
import play.api.Logger

import scala.math.min
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SubscriptionController extends SubscriptionController {
  val subscriptionService: SubscriptionService = SubscriptionService
  val auditService = AuditService
  override val authConnector = AuthConnector
}

trait SubscriptionController extends BaseController with Authorisation {

  val subscriptionService: SubscriptionService
  val auditService : AuditService

  val subscribe = (safeId: String, postcode: String) => Action.async(BodyParsers.parse.json) { implicit request =>
    authorised {
      case Authorised => {

        val subscriptionApplicationBodyJs = request.body.validate[SubscriptionType]
        subscriptionApplicationBodyJs.fold(
          errors => Future.successful(BadRequest(Json.toJson(Error(message = "Request to subscribe application failed with validation errors: " + errors)))),
          subscribeRequest => {
            val acknowledgementRef = generateAcknowledgementRef(safeId)
            val timerContext = AuditService.metrics.startTimer(MetricsEnum.TAVC_SUBSCRIPTION)
            subscriptionService.subscribe(safeId, SubscriptionRequest(acknowledgementRef,subscribeRequest), postcode) map { responseReceived =>
              auditService.sendTAVCSubscriptionEvent(subscribeRequest, safeId, responseReceived, acknowledgementRef)
              auditService.logSubscriptionResponse(responseReceived, "SubscriptionController", "subscribe", safeId)
              val stopContext = timerContext.stop()
              Status(responseReceived.status)(responseReceived.body)
            }
          }
        )
      }
      case NotAuthorised => {
        Logger.warn(s"[SubmissionController] [subscribe] - Received an unauthorised request. safeId is $safeId")
        Future.successful(Forbidden)
      }
    }
  }

  /** Randomly generate acknowledgementReference, must be between 1 and 32 characters long**/
  private def generateAcknowledgementRef(safeId: String): String =  {
    val ackRef = safeId concat  (System.currentTimeMillis / 1000).toString
    ackRef.substring(0, min(ackRef.length(), 31))
  }

}
