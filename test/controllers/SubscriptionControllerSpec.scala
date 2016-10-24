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
import helpers.AuthHelper.Authorities._
import helpers.AuthHelper.AffinityGroups._
import connectors.AuthConnector
import helpers.AuthHelper._
import helpers.TestHelper
import model.SubscriptionResponse
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.BeforeAndAfter
import services.{AuditService, SubscriptionService}

import scala.concurrent.Future

class SubscriptionControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with BeforeAndAfter with TestHelper{

  val mockSubscriptionService = mock[SubscriptionService]


  val subscriptionResponse = SubscriptionResponse("FBUND09889765", "Subscription Request Successful")

  val malformedJson =
    """
      |{
      |{
      |  "statusCode": malformed,
      |  "message": "malformed"}'"
      |}
    """.stripMargin

  implicit val hc = HeaderCarrier()

  class Setup(status: Int, response: Option[JsValue]) {
    when(mockSubscriptionService.subscribe(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, Some(response.getOrElse(Json.toJson(""""""))))))
    object TestController extends SubscriptionController {
      override val subscriptionService = mockSubscriptionService
      override val authConnector = mockAuthConnector
      override val auditService = mockAuditService
    }
  }

  before {
    reset(mockAuthConnector)
  }

  "SubscriptionController" should {
    "use the correct auth connector" in {
      SubscriptionController.authConnector shouldBe AuthConnector
    }
    "use the correct subscription service" in {
      SubscriptionController.subscriptionService shouldBe SubscriptionService
    }
    "use the correct audit service" in {
      SubscriptionController.auditService shouldBe AuditService
    }
  }

  "SubscriptionController.subscibe with a TAVC account with status Activated and confidence level 50" when {

    "subscribe is called" should {

      "return an OK when the full subscription process is completed successfully" in new Setup(OK, None) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestValid.subscriptionType)))
        status(result) shouldBe OK
      }

      "return a Not Found when a Not Found response is returned from the subscribe service" in new Setup(NOT_FOUND, None) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestNotFound.subscriptionType)))
        status(result) shouldBe NOT_FOUND
      }


      "return a BadRequest when a Bad Request response is returned from the subscribe service" in
      new Setup(BAD_REQUEST,  Some(Json.toJson("""{"reason" : "Error 400"}"""))) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
        status(result) shouldBe BAD_REQUEST
      }

      "return an OK when an OK response is returned from the subscribe service" in new Setup(OK, None) {
          setUp(userCL50,organisation)
          val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
            Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
          status(result) shouldBe OK
        }

      "return a NO_CONTENT when a NO_CONTENT response is returned from the subscribe service" in new Setup(NO_CONTENT, None) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
        status(result) shouldBe NO_CONTENT
      }

      "return a CREATED when a CREATED response is returned from the subscribe service" in new Setup(CREATED, None) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
        status(result) shouldBe CREATED
      }

      "return a ServiceUnavailable when a ServiceUnavailable is returned from the subscribe service" in new Setup(SERVICE_UNAVAILABLE,
        Some(Json.toJson("""{"reason" : "Service Unavailable"}"""))) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestServiceUnavailable.subscriptionType)))
        status(result) shouldBe SERVICE_UNAVAILABLE
      }

      "return an Internal Server error when any other response is returned from the subscribe service" in new Setup(INTERNAL_SERVER_ERROR,
        Some(Json.toJson( Json.toJson("""Server error""")))) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestServerError.subscriptionType)))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return a Bad request with malformed JSON" in new Setup(BAD_REQUEST,  Some(Json.toJson("""{"reason" : "Invalid JSON message received"}"""))) {
        setUp(userCL50,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(Json.toJson(malformedJson)))
        status(result) shouldBe BAD_REQUEST
      }
    }
  }

  "SubscriptionController.subscibe with a TAVC account with status NotYetActivated and confidence level 50" when {

    "subscibe is called" should {

      "return a FORBIDDEN response" in new Setup(FORBIDDEN, None) {
        setUp(userCL0,organisation)
        val result = TestController.subscribe(dummyValidSafeID,dummyValidPostcode)(FakeRequest().withBody(Json.toJson(dummySubscriptionRequestValid)))
        status(result) shouldBe FORBIDDEN
      }
    }
  }

}