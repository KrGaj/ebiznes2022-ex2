package api.product

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying post information.
  */
case class ProductResource(id: String, link: String, title: String, body: String)

object ProductResource {
  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
  implicit val format: Format[ProductResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[ProductResource]]
  */
class PostResourceHandler @Inject()(
                                     routerProvider: Provider[ProductRouter],
                                     postRepository: ProductRepository)(implicit ec: ExecutionContext) {

  def create(postInput: ProductFormInput)(
    implicit mc: MarkerContext): Future[ProductResource] = {
    val data = ProductData(ProductId("999"), postInput.title, postInput.body)
    // We don't actually create the post, so return what we have
    postRepository.create(data).map { id =>
      createProductResource(data)
    }
  }

  def lookup(id: String)(
    implicit mc: MarkerContext): Future[Option[ProductResource]] = {
    val postFuture = postRepository.get(ProductId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { productData =>
        createProductResource(productData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[ProductResource]] = {
    postRepository.list().map { productDataList =>
      productDataList.map(postData => createProductResource(postData))
    }
  }

  private def createProductResource(p: ProductData): ProductResource = {
    ProductResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }
}
