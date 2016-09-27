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

package auth

import connectors.AuthConnector
import helpers.AuthHelper.Authorities._
import helpers.AuthHelper._
import org.scalatest.BeforeAndAfter
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._


import scala.concurrent.Future

class AuthorisationSpec extends FakeApplication with UnitSpec with BeforeAndAfter {

  object TestAuthorisation extends Authorisation {
    override val authConnector: AuthConnector = mockAuthConnector
  }

  def authorised(): Future[Result] = TestAuthorisation.authorised {
    case Authorised => Future.successful(Ok)
    case NotAuthorised => Future.successful(Forbidden)
  }

  before {
    reset(mockAuthConnector)
  }

  "Authorisation.authorised" should {

    "Return a FORBIDDEN result when the user no authority and no affinity group" in {
      setUp(None, None)
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return a FORBIDDEN result when the user has Confidence Level L0 and is an Organisation" in {
      setUp(userCL0, Some("Organisation"))
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return a FORBIDDEN result when the user has Confidence Level L0 and is an Agent" in {
      setUp(userCL0, Some("Agent"))
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return a FORBIDDEN result when the user has Confidence Level L0 and there is no Affinity group" in {
      setUp(userCL0, None)
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L50 and is an Organisation" in {
      setUp(userCL50, Some("Organisation"))
      val result = authorised()
      status(result) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L50 and is an Agent" in {
      setUp(userCL50, Some("Agent"))
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L50 and there is no Affinity group" in {
      setUp(userCL50, None)
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L100 and is an Organisation" in {
      setUp(userCL100, Some("Organisation"))
      val result = authorised()
      status(result) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L100 and is an Agent" in {
      setUp(userCL100, Some("Agent"))
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L100 and there is no Affinity group" in {
      setUp(userCL100, None)
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L200 and is an Organisation" in {
      setUp(userCL200, Some("Organisation"))
      val result = authorised()
      status(result) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L200 and is an Agent" in {
      setUp(userCL200, Some("Agent"))
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L200 and there is no Affinity group" in {
      setUp(userCL200, None)
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L300 and is an Organisation" in {
      setUp(userCL300, Some("Organisation"))
      val result = authorised()
      status(result) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L300 and is an Agent" in {
      setUp(userCL300, Some("Agent"))
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L300 and there is no Affinity group" in {
      setUp(userCL300, None)
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L500 and is an Organisation" in {
      setUp(userCL500, Some("Organisation"))
      val result = authorised()
      status(result) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L500 and is an Agent" in {
      setUp(userCL500, Some("Agent"))
      val result = authorised()
      status(result) shouldBe FORBIDDEN

    }

    "Return an OK result when the user has a low Confidence Level L500 and there is no Affinity group" in {
      setUp(userCL500, None)
      val result = authorised()
      status(result) shouldBe FORBIDDEN
    }
  }
}
