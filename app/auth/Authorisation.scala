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

package auth

import play.api.mvc.Result
import connectors.AuthConnector
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Authorisation {

  val authConnector: AuthConnector

  def authorised(f: => AuthorisationResult => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    for {
      authority <- authConnector.getCurrentAuthority()
      affinityGroup <- authConnector.getAffinityGroup(getUri(authority))
      result <- f(mapToAuthResult(affinityGroup, authority))
    } yield result
  }

  private def mapToAuthResult(affinityGroup: Option[String], authority: Option[Authority]): AuthorisationResult = {
    (affinityGroup, authority) match {
      case (Some(affGroup), Some(authRecord)) => {
        (affGroup, authRecord.confidenceLevel) match {
          case ("Organisation", x) if x >= ConfidenceLevel.L50 => Authorised
          case ("Agent", _) => NotAuthorised
          case (_, _) => NotAuthorised
        }
      }
      case (_,_) => NotAuthorised
    }
  }

  private def getUri(authority: Option[Authority]): String = authority.fold("")(_.uri)

}
