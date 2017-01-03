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

import model._
import org.mockito.ArgumentCaptor
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.http.HttpResponse

object TestHelper extends TestHelper

trait TestHelper {

  val friendlyServiceName = "Get tax relief for your investors"
  val portalId = "Default"
  val serviceName = "HMRC-TAVC-ORG"
  val dummyCorrespondenceDetails = CorrespondenceDetailsModel(None,None,None)
  val dummySubscriptionType = SubscriptionType(dummyCorrespondenceDetails)
  val dummySubscriptionRequestValid = SubscriptionRequest("TAVCUNIQUEREF", dummySubscriptionType)
  val dummySubscriptionRequestNotFound = SubscriptionRequest("notfound", dummySubscriptionType)
  val dummySubscriptionRequestDuplicate= SubscriptionRequest("duplicate", dummySubscriptionType)
  val dummySubscriptionRequestServerError = SubscriptionRequest("servererror", dummySubscriptionType)
  val dummySubscriptionRequestServiceUnavailable= SubscriptionRequest("serviceunavailable", dummySubscriptionType)
  val dummySubscriptionRequestMissingRegime = SubscriptionRequest("missingregime", dummySubscriptionType)
  val dummySubscriptionRequestSapNumberMissing = SubscriptionRequest("sapnumbermissing", dummySubscriptionType)
  val dummySubscriptionRequestNotProcessed = SubscriptionRequest("notprocessed", dummySubscriptionType)
  val dummyValidSafeID = "XA0001234567890"
  val dummyInvalidSafeID = "YA0001234567890"
  val dummyValidPostcode = "AA11AA"
  val dummyInvalidPostcode = "INVALID_POSTCODE"
  val dummyValidTavcRegNumber = "XXTAVC000123456"
  val dummyValidProcessingDate = "2016-10-05T09:30:47Z"

  val testRequestPath = "test/path"
  val responseReasonContent = (message: String) => s"""{"reason" : "$message"}"""
  val responseSuccessContent = s"""{"processingDate":"2014-12-17T09:30:47Z","formBundleNumber":"FBUND98763284"}"""

  // logging
  val reasonMessage = (message: String) => s"""{"reason" : "$message"}"""
  val eventCaptor = ArgumentCaptor.forClass(classOf[DataEvent])
  val responseNonContent = HttpResponse(NO_CONTENT)
  val responseBadRequestNoContent = HttpResponse(BAD_REQUEST)
  val responseNotFoundNoContent = HttpResponse(NOT_FOUND)
  val responseServiceUnavailableNoContent = HttpResponse(SERVICE_UNAVAILABLE)
  val responseInternalServerErrorNoContent = HttpResponse(INTERNAL_SERVER_ERROR)
  val responseOtherErrorNoContent = HttpResponse(GATEWAY_TIMEOUT)
  val responseOkSuccess = HttpResponse(OK, Some(Json.parse(responseSuccessContent)))
  val responseOkNocontent = HttpResponse(OK)
  val responseCreatedNocontent = HttpResponse(CREATED)
  val responseBadRequestEtmpDuplicate = HttpResponse(BAD_REQUEST,
    Some(Json.parse(responseReasonContent(EtmpResponseReasons.duplicateSubmission400))))
  val responseServiceUnavailableEtmpNotProcessed = HttpResponse(SERVICE_UNAVAILABLE,
    Some(Json.parse(responseReasonContent(EtmpResponseReasons.notProcessed503))))
  val responseInternalServerErrorEtmpSap = HttpResponse(INTERNAL_SERVER_ERROR,
    Some(Json.parse(responseReasonContent(EtmpResponseReasons.sapError500))))
  val responseInternalServerErrorEtmpRegime = HttpResponse(INTERNAL_SERVER_ERROR,
    Some(Json.parse(responseReasonContent(EtmpResponseReasons.noRegime500))))
  val responseInternalServerErrorEtmp = HttpResponse(INTERNAL_SERVER_ERROR,
    Some(Json.parse(responseReasonContent(EtmpResponseReasons.serverError500))))
  val submissionControllerTestName = "SubmissionController"
  val subscriptionControllerTestName = "SubscriptionController"
  val subscribeTestAction = "subscribe"
  val submitTestAction = "submit"

  val safeId = "XA0001234567890"
  val tavcRefNumber = "XLTAVC000823190"
  val acknowledgementReference = "XE00012345678901477052976"

  val fullContactNameModel = ContactNameModel("fred", Some("Smith"))
  val fullContactDetalsModel = ContactDetailsModel(Some("000001 00000002"),
    Some("100000 20000"), Some("300000 400000"), Some("test@test.com"))
  val fullContactAddress = ContactAddressModel("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "GB", Some("AA1 1AA"))
  val fullCorrespondenceDetails =
    CorrespondenceDetailsModel(Some(fullContactNameModel),Some(fullContactDetalsModel), Some(fullContactAddress))

  val minContactNameModel = ContactNameModel("fred", None)
  val minContactDetailsModel = ContactDetailsModel(None, None, None, None)
  val minContactAddress = ContactAddressModel("Line 1", "Line 2", None, None, "GB", None)
  val minCorrespondenceDetails =
    CorrespondenceDetailsModel(Some(minContactNameModel),Some(minContactDetailsModel), Some(minContactAddress))

  val noCorrespondenceModels =
    CorrespondenceDetailsModel(None,None, None)

  val etmpSuccessResponse = Json.parse(
    s"""
       |{
       |  "processingDate": "$dummyValidProcessingDate",
       |  "tavcRegNumber": "$dummyValidTavcRegNumber"
       |}
    """.stripMargin
  )

  val etmpFailureResponse = Json.parse(
    """
      |{
      |  "Message": "An Error Message"
      |}
    """.stripMargin
  )

  val ggAdminFailureResponse = Json.parse(
    """
      |{
      |  "Message": "An Error Message"
      |}
    """.stripMargin
  )

  val ggEnrolFailureResponse = Json.parse(
    """
      |{
      |  "Message": "An Error Message"
      |}
    """.stripMargin
  )

  val authenticatorFailureResponse = Json.parse(
    """
      |{
      |  "Message": "An Error Message"
      |}
    """.stripMargin
  )
}
