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

import uk.gov.hmrc.play.config.ServicesConfig

class TestAppConfig extends AppConfig  with ServicesConfig{
  override lazy val authURL = "auth"
  override lazy val authenticatorURL = baseUrl("gg-authentication")
  override lazy val ggAdminURL = "government-gateway-admin"
  override lazy val ggURL = "government-gateway"
  override lazy val desURL = "des"
  override lazy val desEnvironment = "des-environment"
  override lazy val desToken = "des-token"
}
