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
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AuthorisationSpec extends FakeApplication with UnitSpec {

  object TestAuthorisation extends Authorisation {
    override val authConnector: AuthConnector = mockAuthConnector
  }

  def authorised(): Future[Result] = TestAuthorisation.authorised {
    case Authorised => Future.successful(Ok)
    case NotAuthorised => Future.successful(Forbidden)
  }

  "Authorisation.authorised" should {

    "Return a FORBIDDEN result when the user no authority" in {
      mockGetAuthorityResponse(None)
      status(authorised()) shouldBe FORBIDDEN
    }

    "Return a FORBIDDEN result when the user has Confidence Level L0" in {
      mockGetAuthorityResponse(userCL0)
      status(authorised()) shouldBe FORBIDDEN
    }

    "Return an OK result when the user has a low Confidence Level L50" in {
      mockGetAuthorityResponse(userCL50)
      status(authorised()) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L100" in {
      mockGetAuthorityResponse(userCL100)
      status(authorised()) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L200" in {
      mockGetAuthorityResponse(userCL200)
      status(authorised()) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L300" in {
      mockGetAuthorityResponse(userCL300)
      status(authorised()) shouldBe OK
    }

    "Return an OK result when the user has a low Confidence Level L500" in {
      mockGetAuthorityResponse(userCL500)
      status(authorised()) shouldBe OK
    }
  }
}
