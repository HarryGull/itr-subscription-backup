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

package services

import common.{AuditConstants, ResponseConstants}
import helpers.{EtmpResponseReasons, TestHelper}
import model.SubscriptionType
import org.mockito.{ArgumentCaptor, Matchers}
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.WithFakeApplication
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.play.audit.model.{Audit, DataEvent}
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import metrics.{Metrics, MetricsEnum}
import org.scalatest.BeforeAndAfter

class AuditServiceSpec extends UnitSpec with MockitoSugar with AppName with WithFakeApplication with BeforeAndAfter with TestHelper {

  val auditMock = mock[Audit]
  val metricsMock = mock[Metrics]
  val auditMockResponse = mock[(DataEvent) => Unit]
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("testID")))
  implicit val rh: RequestHeader = FakeRequest("GET", testRequestPath)


  object TestAuditService extends AuditService with AppName {
    override val audit = auditMock
    override val metrics = metricsMock
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val rh: RequestHeader = FakeRequest("GET", testRequestPath)

    when(auditMock.sendDataEvent).thenReturn(auditMockResponse)
  }

  object TestAuditServiceWithCustomLogFormat extends AuditService with AppName {
    override val audit = auditMock
    override val metrics = metricsMock
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val rh: RequestHeader = FakeRequest("GET", testRequestPath)

    // override the logging message format to customise and test it
    override val logMessageFormat = (controller: String, controllerAction: String, dayOfWeek: String, statusCode: String, eventMessage: String) =>
      s"Subscribe Audit event recored on $dayOfWeek for [${controller + "/" + controllerAction}]. StatusCode was: [$statusCode]. Event Status is: $eventMessage"

    val expectedCustomLogFormatSuccessMessageFriday10Count =
      "Subscribe Audit event recored on Friday for [SubmissionController/subscribe]. StatusCode was: [200]. Event Status is: Success"
    //Subscribe Audit event recorded on Friday for [SubmissionController/subscribe]. StatusCode was: [200]. Event Count is: Success

    when(auditMock.sendDataEvent).thenReturn(auditMockResponse)
  }

  before {
    reset(metricsMock)
  }

  "Calling AuditService.sendTAVCSubscriptionEvent and status OK" when {
    "the request body SubscriptionType is fully populated" should {
      "perform the underlying TXM explicit audit with the actualDataEvent populated as expected" in {
        TestAuditService.sendTAVCSubscriptionEvent(SubscriptionType(fullCorrespondenceDetails), safeId,
          responseNonContent, acknowledgementReference)

        verify(auditMockResponse, atLeastOnce()).apply(eventCaptor.capture())
        val actualDataEvent = eventCaptor.getValue
        actualDataEvent.auditSource shouldBe appName
        actualDataEvent.auditType shouldBe AuditConstants.subscibeAuditType
        actualDataEvent.tags("transactionName") shouldBe AuditConstants.transactionName
        actualDataEvent.tags("path") shouldBe testRequestPath
        actualDataEvent.detail("statusCode") shouldBe responseNonContent.status.toString
        actualDataEvent.detail("failureReason") shouldBe AuditConstants.notApplicable
        actualDataEvent.detail("safeId") shouldBe safeId
        actualDataEvent.detail("acknowledgementReference") shouldBe acknowledgementReference
        actualDataEvent.detail("forename") shouldBe fullContactNameModel.name1
        actualDataEvent.detail("surname") shouldBe fullContactNameModel.name2.getOrElse("")
        actualDataEvent.detail("phoneNumber") shouldBe fullContactDetalsModel.phoneNumber.getOrElse("")
        actualDataEvent.detail("mobileNumber") shouldBe fullContactDetalsModel.mobileNumber.getOrElse("")
        actualDataEvent.detail("emailAddress") shouldBe fullContactDetalsModel.emailAddress.getOrElse("")
        actualDataEvent.detail("addressLine1") shouldBe fullContactAddress.addressLine1
        actualDataEvent.detail("addressLine2") shouldBe fullContactAddress.addressLine2
        actualDataEvent.detail("addressLine3") shouldBe fullContactAddress.addressLine3.getOrElse("")
        actualDataEvent.detail("addressLine4") shouldBe fullContactAddress.addressLine4.getOrElse("")
        actualDataEvent.detail("postCode") shouldBe fullContactAddress.postalCode.getOrElse("")
        actualDataEvent.detail("country") shouldBe fullContactAddress.countryCode
      }
    }
  }

  "Calling AuditService.sendTAVCSubscriptionEvent and status OK" when {
    "the request body SubscriptionType has no models present" should {
      "perform the underlying TXM explicit audit with the actualDataEvent populated as expected" in {
        TestAuditService.sendTAVCSubscriptionEvent(SubscriptionType(noCorrespondenceModels), safeId,
          responseOkSuccess, acknowledgementReference)

        verify(auditMockResponse, atLeastOnce()).apply(eventCaptor.capture())
        val actualDataEvent = eventCaptor.getValue
        actualDataEvent.auditSource shouldBe appName
        actualDataEvent.auditType shouldBe AuditConstants.subscibeAuditType
        actualDataEvent.tags("transactionName") shouldBe AuditConstants.transactionName
        actualDataEvent.tags("path") shouldBe testRequestPath
        actualDataEvent.detail("statusCode") shouldBe responseOkSuccess.status.toString
        actualDataEvent.detail("failureReason") shouldBe AuditConstants.notApplicable
        actualDataEvent.detail("safeId") shouldBe safeId
        actualDataEvent.detail("acknowledgementReference") shouldBe acknowledgementReference
        actualDataEvent.detail("forename") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("surname") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("phoneNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("mobileNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("emailAddress") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine1") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine2") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine3") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine4") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("postCode") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("country") shouldBe AuditConstants.noValueProvided
      }
    }
  }

  "Calling AuditService.sendTAVCSubscriptionEvent and status OK" when {
    "the request body SubscriptionType has all models present with any optionals values not populated" should {
      "perform the underlying TXM explicit audit with the actualDataEvent populated as expected" in {
        TestAuditService.sendTAVCSubscriptionEvent(SubscriptionType(minCorrespondenceDetails), safeId,
          responseOkSuccess, acknowledgementReference)

        verify(auditMockResponse, atLeastOnce()).apply(eventCaptor.capture())
        val actualDataEvent = eventCaptor.getValue
        actualDataEvent.auditSource shouldBe appName
        actualDataEvent.auditType shouldBe AuditConstants.subscibeAuditType
        actualDataEvent.tags("transactionName") shouldBe AuditConstants.transactionName
        actualDataEvent.tags("path") shouldBe testRequestPath
        actualDataEvent.detail("statusCode") shouldBe responseOkSuccess.status.toString
        actualDataEvent.detail("failureReason") shouldBe AuditConstants.notApplicable
        actualDataEvent.detail("safeId") shouldBe safeId
        actualDataEvent.detail("acknowledgementReference") shouldBe acknowledgementReference
        actualDataEvent.detail("forename") shouldBe minContactNameModel.name1
        actualDataEvent.detail("surname") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("phoneNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("mobileNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("emailAddress") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine1") shouldBe minContactAddress.addressLine1
        actualDataEvent.detail("addressLine2") shouldBe minContactAddress.addressLine2
        actualDataEvent.detail("addressLine3") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine4") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("postCode") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("country") shouldBe minContactAddress.countryCode
      }
    }
  }

  "Calling AuditService.sendTAVCSubscriptionEvent with status Bad Request" when {
    "the response body has no etmp failure reason" should {
      "perform the underlying TXM audit with the actualDataEvent and failure reason populated with default failure reason as expected" in {
        TestAuditService.sendTAVCSubscriptionEvent(SubscriptionType(minCorrespondenceDetails), safeId,
          responseBadRequestNoContent, acknowledgementReference)

        verify(auditMockResponse, atLeastOnce()).apply(eventCaptor.capture())
        val actualDataEvent = eventCaptor.getValue
        actualDataEvent.auditSource shouldBe appName
        actualDataEvent.auditType shouldBe AuditConstants.subscibeAuditType
        actualDataEvent.tags("transactionName") shouldBe AuditConstants.transactionName
        actualDataEvent.tags("path") shouldBe testRequestPath
        actualDataEvent.detail("statusCode") shouldBe responseBadRequestNoContent.status.toString
        // Default Failure Reason as no response message:
        actualDataEvent.detail("failureReason") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("safeId") shouldBe safeId
        actualDataEvent.detail("acknowledgementReference") shouldBe acknowledgementReference
        actualDataEvent.detail("forename") shouldBe minContactNameModel.name1
        actualDataEvent.detail("surname") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("phoneNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("mobileNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("emailAddress") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine1") shouldBe minContactAddress.addressLine1
        actualDataEvent.detail("addressLine2") shouldBe minContactAddress.addressLine2
        actualDataEvent.detail("addressLine3") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine4") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("postCode") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("country") shouldBe minContactAddress.countryCode
      }
    }
  }

  "Calling AuditService.sendTAVCSubscriptionEvent with status Bad Request" when {
    "the response body has the etmp bad request duplicate failure reason" should {
      "perform the underlying TXM explicit audit with the actualDataEvent and failure reason populated as expected" in {
        TestAuditService.sendTAVCSubscriptionEvent(SubscriptionType(minCorrespondenceDetails), safeId,
          responseBadRequestEtmpDuplicate, acknowledgementReference)

        verify(auditMockResponse, atLeastOnce()).apply(eventCaptor.capture())
        val actualDataEvent = eventCaptor.getValue
        actualDataEvent.auditSource shouldBe appName
        actualDataEvent.auditType shouldBe AuditConstants.subscibeAuditType
        actualDataEvent.tags("transactionName") shouldBe AuditConstants.transactionName
        actualDataEvent.tags("path") shouldBe testRequestPath
        actualDataEvent.detail("statusCode") shouldBe responseBadRequestEtmpDuplicate.status.toString
        // Reason From Response message:
        actualDataEvent.detail("failureReason") shouldBe EtmpResponseReasons.duplicateSubmission400
        actualDataEvent.detail("safeId") shouldBe safeId
        actualDataEvent.detail("acknowledgementReference") shouldBe acknowledgementReference
        actualDataEvent.detail("forename") shouldBe minContactNameModel.name1
        actualDataEvent.detail("surname") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("phoneNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("mobileNumber") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("emailAddress") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine1") shouldBe minContactAddress.addressLine1
        actualDataEvent.detail("addressLine2") shouldBe minContactAddress.addressLine2
        actualDataEvent.detail("addressLine3") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("addressLine4") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("postCode") shouldBe AuditConstants.noValueProvided
        actualDataEvent.detail("country") shouldBe minContactAddress.countryCode
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is Ok with no content" should {
      "log the expected message and call the incrementSuccessCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseOkNocontent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.success
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseOkNocontent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementSuccessCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling TestAuditServiceWithCustomLogFormat.logSubscriptionResponse " when {
    "the status is Ok with no content" should {
      "log the expected message in the overriden custom format and call the incrementSuccessCounter metric" in {
        val loggedMessage = TestAuditServiceWithCustomLogFormat.logSubscriptionResponse(responseOkNocontent,
          submissionControllerTestName, subscribeTestAction, "Friday")

        val expectedReason = ResponseConstants.success
        loggedMessage shouldBe TestAuditServiceWithCustomLogFormat.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, "Friday", responseOkNocontent.status.toString, "Success")

        loggedMessage shouldBe TestAuditServiceWithCustomLogFormat.expectedCustomLogFormatSuccessMessageFriday10Count
        verify(metricsMock, times(1)).incrementSuccessCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is Created with no content" should {
      "log the expected message and call the incrementSuccessCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseCreatedNocontent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.success
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseCreatedNocontent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementSuccessCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is OK with no content" should {
      "log the expected message and call the incrementSuccessCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseNonContent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.success
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseNonContent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementSuccessCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is OK with Etmp response message" should {
      "log the expected message and call the incrementSuccessCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseOkSuccess,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.success
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseOkSuccess.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementSuccessCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is bad request with no content" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseBadRequestNoContent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.defaultBadRequest
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseBadRequestNoContent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is bad request with Etmp response content duplicate" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseBadRequestEtmpDuplicate,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = EtmpResponseReasons.duplicateSubmission400
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseBadRequestEtmpDuplicate.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is not found with no content" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseNotFoundNoContent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.defaultNotFound
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseNotFoundNoContent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is server unavailable with no content" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseServiceUnavailableNoContent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.defaultServiceUnavailable
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseServiceUnavailableNoContent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is server unavailable with Etmp response not processed" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseServiceUnavailableEtmpNotProcessed,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = EtmpResponseReasons.notProcessed503
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseServiceUnavailableEtmpNotProcessed.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }


  "Calling AuditService.logSubscriptionResponse " when {
    "the status is internal server error with no content" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseInternalServerErrorNoContent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.defaultInternalServerError
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseInternalServerErrorNoContent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscriptionResponse " when {
    "the status is internal server error with Etmp sap error response" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseInternalServerErrorEtmpSap,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = EtmpResponseReasons.sapError500
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseInternalServerErrorEtmpSap.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscription" when {
    "the status is internal server error with Etmp response" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseInternalServerErrorEtmp,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = EtmpResponseReasons.serverError500
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseInternalServerErrorEtmp.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }

  "Calling AuditService.logSubscription" when {
    "the status is internal server error with Etmp no regime" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseInternalServerErrorEtmpRegime,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = EtmpResponseReasons.noRegime500
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseInternalServerErrorEtmpRegime.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }


  "Calling AuditService.logSubscriptionResponse " when {
    "the status is other error with success content" should {
      "log the expected message and call the incrementFailedCounter metric" in {
        val loggedMessage = TestAuditService.logSubscriptionResponse(responseOtherErrorNoContent,
          submissionControllerTestName, subscribeTestAction, safeId)
        val expectedReason = ResponseConstants.defaultOther
        loggedMessage shouldBe TestAuditService.logMessageFormat(submissionControllerTestName,
          subscribeTestAction, safeId, responseOtherErrorNoContent.status.toString, expectedReason)

        verify(metricsMock, times(1)).incrementFailedCounter(MetricsEnum.TAVC_SUBSCRIPTION)
      }
    }
  }
}
