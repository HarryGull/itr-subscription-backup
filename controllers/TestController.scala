package controllers

import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import scala.concurrent.Future

object TestController extends TestController

trait TestController extends BaseController {

	def hello(): Action[AnyContent] = Action.async { implicit request =>
		Future.successful(Ok("Hello world"))
	}
}
