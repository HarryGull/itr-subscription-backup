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

import connectors.AuthConnector
import helpers.AuthHelper._
import helpers.AuthHelper.Authorities._
import helpers.FakeRequestHelper
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class TestControllerSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  object TestTestController extends TestController {
    override val authConnector: AuthConnector = mockAuthConnector
  }

  "TestController" should {
    "use the correct auth connector" in {
      TestController.authConnector shouldBe AuthConnector
    }
  }

  "A user with No Authority and no affinity group" when {

    "calling TestController.Hello()" should {

      "return a FORBIDDEN response" in {
        setUp(None, None)
        status(TestTestController.hello()(fakeRequest)) shouldBe Status.FORBIDDEN
      }
    }
  }

  "A user with a Confidence Level of CL50 and is an organisation" when {

    "calling TestController.Hello()" should {

      setUp(userCL50, Some("Organisation"))
      val result = await(TestTestController.hello()(fakeRequest))

      "return an OK response" in {
        status(result) shouldBe Status.OK
      }

      "return the content type Application/Json" in {
        contentType(result) shouldBe Some("application/json")
      }

      "Return the correct json response" in {
        bodyOf(result) shouldBe """[{"key":"test","identifiers":[{"key":"test","value":"test"}],"state":"test"}]"""
      }
    }
  }
}
