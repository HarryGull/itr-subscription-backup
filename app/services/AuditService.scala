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

import common.{AuditConstants, ResponseConstants}
import config.MicroserviceAuditConnector
import model.SubscriptionType
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.audit.model.{Audit, DataEvent, EventTypes}
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import play.api.http.Status
import play.api.Logger
import metrics.{Metrics, MetricsEnum}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

object AuditService extends AuditService with AppName {
  val audit = new Audit(appName, MicroserviceAuditConnector)
  val metrics = Metrics
  //override val logMessageFormat = (controller:String, controllerAction:String, safeId:String,  statusCode:String, message:String) =>
    //s"[$controller] [$controllerAction] [$safeId] [$statusCode] - $message"
}

trait AuditService {
  this: AppName =>

  val audit: Audit
  val metrics: Metrics
  val logMessageFormat = (controller:String, controllerAction:String, safeId:String,  statusCode:String, message:String) =>
    s"[$controller] [$controllerAction] [$safeId] [$statusCode] - $message"

  def sendTAVCSubscriptionEvent(subscriptionType: SubscriptionType, safeId: String,
                                responseReceived: HttpResponse, acknowledgementRef: String)
                               (implicit hc: HeaderCarrier, rh: RequestHeader): Unit = {

    val failureReason: String = if(checkResponseSuccess(responseReceived.status)) AuditConstants.notApplicable else
        getResponseReason(responseReceived).fold(AuditConstants.noValueProvided)(_.toString)

    val np = AuditConstants.noValueProvided

    audit.sendDataEvent(
      DataEvent.apply(
        appName,
        AuditConstants.subscibeAuditType,
        tags = hc.toAuditTags(AuditConstants.transactionName, rh.path),
        detail = hc.toAuditDetails(Map(
          "statusCode" -> responseReceived.status.toString,
          "failureReason" -> failureReason,
          "safeId" -> safeId,
          "acknowledgementReference" -> acknowledgementRef,
          "forename" -> subscriptionType.correspondenceDetails.contactName.fold(np)(_.name1),
          "surname" -> subscriptionType.correspondenceDetails.contactName.fold(np)(_.name2.getOrElse(np)),
          "phoneNumber" -> subscriptionType.correspondenceDetails.contactDetails.fold(np)(_.phoneNumber.getOrElse(np)),
          "mobileNumber" -> subscriptionType.correspondenceDetails.contactDetails.fold(np)(_.mobileNumber.getOrElse(np)),
          "emailAddress" -> subscriptionType.correspondenceDetails.contactDetails.fold(np)(_.emailAddress.getOrElse(np)),
          "addressLine1" -> subscriptionType.correspondenceDetails.contactAddress.fold(np)(_.addressLine1),
          "addressLine2" -> subscriptionType.correspondenceDetails.contactAddress.fold(np)(_.addressLine2),
          "addressLine3" -> subscriptionType.correspondenceDetails.contactAddress.fold(np)(_.addressLine3.getOrElse(np)),
          "addressLine4" -> subscriptionType.correspondenceDetails.contactAddress.fold(np)(_.addressLine4.getOrElse(np)),
          "postCode" -> subscriptionType.correspondenceDetails.contactAddress.fold(np)(_.postalCode.getOrElse(np)),
          "country" -> subscriptionType.correspondenceDetails.contactAddress.fold(np)(_.countryCode)).toSeq: _*)
      )
    )
  }

  def logSubscriptionResponse(responseReceived: HttpResponse, controller: String, controllerAction: String, safeId: String): String = {

    val statusCode = responseReceived.status
    val message = getResponseReason(responseReceived).fold(getDefaultStatusMessage(statusCode))(_.toString)
    val logMessage = logMessageFormat(controller, controllerAction, safeId, statusCode.toString, message)

    checkResponseSuccess(responseReceived.status) match {
      case true =>
        metrics.incrementSuccessCounter(MetricsEnum.TAVC_SUBSCRIPTION)
        Logger.info(logMessage)
      case _ =>
        metrics.incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
        Logger.warn(logMessage)
    }

    logMessage
  }

  private def getResponseReason(response: HttpResponse): Option[String] = {
    Try {
      if (response.body.nonEmpty && response.body.contains("reason"))
        Some((response.json \ "reason").as[String])
      else None
    } match {
      case Success(result) => result
      case Failure(_) => None
    }
  }


  private def checkResponseSuccess(statusCode: Int): Boolean = {
    statusCode.toString.startsWith("2")
  }

  private def getDefaultStatusMessage(statusCode: Int): String = {
    if (checkResponseSuccess(statusCode))
      ResponseConstants.success
    else {
      statusCode match {
        case Status.NOT_FOUND => ResponseConstants.defaultNotFound
        case Status.BAD_REQUEST => ResponseConstants.defaultBadRequest
        case Status.SERVICE_UNAVAILABLE => ResponseConstants.defaultServiceUnavailable
        case Status.INTERNAL_SERVER_ERROR => ResponseConstants.defaultInternalServerError
        case _ => ResponseConstants.defaultOther
      }
    }
  }

}
