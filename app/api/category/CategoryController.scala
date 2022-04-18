package api.category

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class CategoryFormInput(title: String, body: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class CategoryController @Inject()(cc: CategoryControllerComponents)(
  implicit ec: ExecutionContext)
  extends CategoryBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[CategoryFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(CategoryFormInput.apply)(CategoryFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = CategoryAction.async { implicit request =>
    logger.trace("index: ")
    categoryResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = CategoryAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: String): Action[AnyContent] = CategoryAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      categoryResourceHandler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  private def processJsonPost[A]()(
    implicit request: CategoryRequest[A]): Future[Result] = {
    def failure(badForm: Form[CategoryFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CategoryFormInput) = {
      categoryResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

