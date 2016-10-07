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

package helpers

import model.{CorrespondenceDetailsModel, SubscriptionRequest, SubscriptionType}
import play.api.libs.json.Json

object Constants extends Constants

trait Constants {

  val friendlyServiceName = "Tax Advantaged Venture Capital Schemes Enrolment"
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
  val dummyValidPostcode = "SY76TA"
  val dummyInvalidPostcode = "INVALID_POSTCODE"
  val dummyValidTavcRegNumber = "XXTAVC000123456"
  val dummyValidProcessingDate = "2016-10-05T09:30:47Z"

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