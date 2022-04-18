package api.product

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class ProductFormInput(title: String, body: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class ProductController @Inject()(cc: ProductControllerComponents)(
  implicit ec: ExecutionContext)
  extends ProductBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[ProductFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(ProductFormInput.apply)(ProductFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("index: ")
    productResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: String): Action[AnyContent] = ProductAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      productResourceHandler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  private def processJsonPost[A]()(
    implicit request: ProductRequest[A]): Future[Result] = {
    def failure(badForm: Form[ProductFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: ProductFormInput) = {
      productResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

