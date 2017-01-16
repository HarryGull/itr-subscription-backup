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

package config

import com.google.inject.AbstractModule
import connectors._
import metrics.{Metrics, MetricsImpl}
import services._
import uk.gov.hmrc.play.http.ws.WSHttp

class DIModule extends AbstractModule {

  protected def configure() {

    // HTTP
    bind(classOf[WSHttp]).to(classOf[Http])

    // Config
    bind(classOf[AppConfig]).to(classOf[MicroserviceAppConfig])

    // Connectors
    bind(classOf[Metrics]).to(classOf[MetricsImpl])
    bind(classOf[AuthConnector]).to(classOf[AuthConnectorImpl])
    bind(classOf[AuthenticatorConnector]).to(classOf[AuthenticatorConnectorImpl])
    bind(classOf[GovernmentGatewayAdminConnector]).to(classOf[GovernmentGatewayAdminConnectorImpl])
    bind(classOf[GovernmentGatewayConnector]).to(classOf[GovernmentGatewayConnectorImpl])
    bind(classOf[SubscriptionETMPConnector]).to(classOf[SubscriptionETMPConnectorImpl])

    // Services
    bind(classOf[AuditService]).to(classOf[AuditServiceImpl])
    bind(classOf[SubscriptionService]).to(classOf[SubscriptionServiceImpl])
  }

}
