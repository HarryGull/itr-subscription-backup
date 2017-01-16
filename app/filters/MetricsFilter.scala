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
import com.kenshoo.play.metrics.{MetricsFilter => Metrics}
import play.api.inject.Injector
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.Future

class MetricsFilter @Inject()(injector: Injector)(implicit val mat: Materializer) extends Filter {
  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] =
    injector.instanceOf[Metrics].apply(f)(rh)
}