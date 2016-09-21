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
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeApplication
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

class AuthorisationSpec extends FakeApplication with UnitSpec with MockitoSugar {

  implicit val hc = HeaderCarrier()
  val mockAuthConnector = mock[AuthConnector]

  object TestAuthorisation extends Authorisation {
    override val authConnector: AuthConnector = mockAuthConnector
  }

  "Authorisation.authorised" should {
    "Return an Authorised result when the user has an active TAVC account" in {
      //when(mockAuthConnector.getCurrentAuthority())
    }
  }

}
