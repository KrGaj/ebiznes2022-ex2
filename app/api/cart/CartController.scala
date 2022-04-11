package api.cart

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._
import v1.post.{PostBaseController, PostControllerComponents, PostFormInput, PostRequest}

import scala.concurrent.{ExecutionContext, Future}

case class CartFormInput(title: String, body: String, productId: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class CartController @Inject()(cc: CartControllerComponents)(
  implicit ec: ExecutionContext)
  extends CartBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[CartFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text,
        "productId" -> nonEmptyText
      )(CartFormInput.apply)(CartFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = CartAction.async { implicit request =>
    logger.trace("index: ")
    cartResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = CartAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: String): Action[AnyContent] = CartAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      cartResourceHandler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  private def processJsonPost[A]()(
    implicit request: PostRequest[A]): Future[Result] = {
    def failure(badForm: Form[CartFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CartFormInput) = {
      cartResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

