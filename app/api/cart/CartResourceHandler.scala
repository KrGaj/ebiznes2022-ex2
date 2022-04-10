package api.cart

import javax.inject.{Inject, Provider}
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import v1.post.PostFormInput

/**
  * DTO for displaying post information.
  */
case class CartResource(id: String, link: String, title: String, body: String)

object CartResource {
  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
  implicit val format: Format[CartResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[CartResource]]
  */
class PostResourceHandler @Inject()(
                                     routerProvider: Provider[PostRouter],
                                     cartRepository: CartRepository)(implicit ec: ExecutionContext) {

  def create(postInput: PostFormInput)(
    implicit mc: MarkerContext): Future[CartResource] = {
    val data = CartData(CartId("999"), postInput.title, postInput.body)
    // We don't actually create the post, so return what we have
    cartRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(
    implicit mc: MarkerContext): Future[Option[CartResource]] = {
    val postFuture = cartRepository.get(CartId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[CartResource]] = {
    cartRepository.list().map { postDataList =>
      postDataList.map(postData => createPostResource(postData))
    }
  }

  private def createPostResource(p: CartData): CartResource = {
    CartResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}

