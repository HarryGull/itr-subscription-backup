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

package controllers

import com.codahale.metrics.Timer
import common.GetSubscriptionResponses
import helpers.{AuthHelper, TestHelper}
import metrics.{Metrics, MetricsEnum, MetricsImpl}
import model.{SubscriptionResponse, SubscriptionType}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.OneAppPerSuite
import play.api.mvc.RequestHeader
import services.{AuditService, SubscriptionService}

import scala.concurrent.Future

class SubscriptionControllerSpec extends UnitSpec with MockitoSugar with OneAppPerSuite with BeforeAndAfter with TestHelper with AuthHelper {

  val mockMetrics = mock[Metrics]
  val mockContext = mock[Timer.Context]
  val mockAuditService = new AuditService {
    override def sendTAVCSubscriptionEvent(subscriptionType: SubscriptionType, safeId: String, responseReceived: HttpResponse, acknowledgementRef: String)
                                          (implicit hc: HeaderCarrier, rh: RequestHeader) = {}
    override def logSubscriptionResponse(responseReceived: HttpResponse, controller: String, controllerAction: String, safeId: String) = "test"
    override val metrics: Metrics = mockMetrics
  }

  val mockSubscriptionService = mock[SubscriptionService]

  val testController = new SubscriptionController(mockSubscriptionService, mockAuditService, mockAuthConnector)

  val subscriptionResponse = SubscriptionResponse("FBUND09889765", "Subscription Request Successful")

  val malformedJson =
    """
      |{
      |{
      |  "statusCode": malformed,
      |  "message": "malformed"}'"
      |}
    """.stripMargin

  class Setup(status: Int, response: Option[JsValue]) {
    when(mockSubscriptionService.subscribe(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, Some(response.getOrElse(Json.toJson(""""""))))))
    when(mockMetrics.startTimer(Matchers.any())).thenReturn(mockContext)
    when(mockContext.stop()).thenReturn(0)
  }

  class SetupGetSubscription(status: Int, response: Option[JsValue]) {
    when(mockSubscriptionService.getSubscription(Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, Some(response.getOrElse(Json.toJson(""""""))))))
  }

  before {
    reset(mockAuthConnector)
  }

  "SubscriptionController.subscibe with a TAVC account with status Activated and confidence level 50" when {

    "subscribe is called" should {

      "return an OK when the full subscription process is completed successfully" in new Setup(OK, None) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestValid.subscriptionType)))
        status(result) shouldBe OK
      }

      "return a Not Found when a Not Found response is returned from the subscribe service" in new Setup(NOT_FOUND, None) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestNotFound.subscriptionType)))
        status(result) shouldBe NOT_FOUND
      }


      "return a BadRequest when a Bad Request response is returned from the subscribe service" in
      new Setup(BAD_REQUEST,  Some(Json.toJson("""{"reason" : "Error 400"}"""))) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
        status(result) shouldBe BAD_REQUEST
      }

      "return an OK when an OK response is returned from the subscribe service" in new Setup(OK, None) {
          setUp(Authorities.userCL50, AffinityGroups.organisation)
          val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
            Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
          status(result) shouldBe OK
        }

      "return a NO_CONTENT when a NO_CONTENT response is returned from the subscribe service" in new Setup(NO_CONTENT, None) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
        status(result) shouldBe NO_CONTENT
      }

      "return a CREATED when a CREATED response is returned from the subscribe service" in new Setup(CREATED, None) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestDuplicate.subscriptionType)))
        status(result) shouldBe CREATED
      }

      "return a ServiceUnavailable when a ServiceUnavailable is returned from the subscribe service" in new Setup(SERVICE_UNAVAILABLE,
        Some(Json.toJson("""{"reason" : "Service Unavailable"}"""))) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestServiceUnavailable.subscriptionType)))
        status(result) shouldBe SERVICE_UNAVAILABLE
      }

      "return an Internal Server error when any other response is returned from the subscribe service" in new Setup(INTERNAL_SERVER_ERROR,
        Some(Json.toJson( Json.toJson("""Server error""")))) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(
          Json.toJson(dummySubscriptionRequestServerError.subscriptionType)))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return a Bad request with malformed JSON" in new Setup(BAD_REQUEST,  Some(Json.toJson("""{"reason" : "Invalid JSON message received"}"""))) {
        setUp(Authorities.userCL50, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode).apply(FakeRequest().withBody(Json.toJson(malformedJson)))
        status(result) shouldBe BAD_REQUEST
      }
    }
  }

  "SubscriptionController.subscibe with a TAVC account with status NotYetActivated and confidence level 50" when {

    "subscibe is called" should {

      "return a FORBIDDEN response" in new Setup(FORBIDDEN, None) {
        setUp(Authorities.userCL0, AffinityGroups.organisation)
        val result = testController.subscribe(dummyValidSafeID,dummyValidPostcode)(FakeRequest().withBody(Json.toJson(dummySubscriptionRequestValid)))
        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "SubscriptionController.getSubscription with a valid Tavc Rreference Number" when {
    "return a ServiceUnavailable when a ServiceUnavailable is returned from the subscription service" in new SetupGetSubscription(SERVICE_UNAVAILABLE,
      Some(Json.toJson("""{"reason" : "Service Unavailable"}"""))) {
      setUp(Authorities.userCL50, AffinityGroups.organisation)
      val result = testController.getSubscription(dummyValidTavcRegNumber).apply(FakeRequest())
      status(result) shouldBe SERVICE_UNAVAILABLE
    }
  }

  "SubscriptionController.getSubscription with a valid Tavc Rreference Number" when {
    "return a Bad request when a BadRequest is returned from the subscription service" in new SetupGetSubscription(BAD_REQUEST,
      Some(Json.toJson("""{"reason" : "Invalid JSON message received"}"""))) {
      setUp(Authorities.userCL50, AffinityGroups.organisation)
      val result = testController.getSubscription(dummyValidTavcRegNumber).apply(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }
  }

  "SubscriptionController.getSubscription with a valid Tavc Rreference Number" when {
    "return the full expected json response request when an OK and matching record is returned from the subscription service" in new SetupGetSubscription(OK,
      Some(GetSubscriptionResponses.getSubFull)) {
      setUp(Authorities.userCL50, AffinityGroups.organisation)
      val result = testController.getSubscription(dummyValidTavcRegNumber).apply(FakeRequest())
      status(result) shouldBe OK
    }
  }

}
