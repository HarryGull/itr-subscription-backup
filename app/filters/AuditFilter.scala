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

package filters

import akka.stream.Materializer
import com.google.inject.Inject
import connectors.AuditConnector
import controllers.ControllerConfiguration
import play.api.mvc.EssentialFilter
import uk.gov.hmrc.play.audit.filters.{AuditFilter => Audit}
import uk.gov.hmrc.play.config.{AppName, RunMode}

class AuditFilter @Inject()(controllerConfiguration: ControllerConfiguration, override val auditConnector: AuditConnector)(implicit val mat: Materializer)
  extends EssentialFilter with Audit with RunMode with AppName {

  override def controllerNeedsAuditing(controllerName: String): Boolean = controllerConfiguration.paramsForController(controllerName).needsAuditing

}
