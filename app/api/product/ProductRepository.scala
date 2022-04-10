package api.product

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class ProductData(id: ProductId, title: String, body: String)

class ProductId private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object ProductId {
  def apply(raw: String): ProductId = {
    require(raw != null)
    new ProductId(Integer.parseInt(raw))
  }
}

class ProductExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait ProductRepository {
  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId]

  def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]]

  def get(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class ProductRepositoryImpl @Inject()()(implicit ec: ProductExecutionContext)
  extends ProductRepository {

  private val logger = Logger(this.getClass)

  private val postList = List(
    ProductData(ProductId("1"), "title 1", "product 1"),
    ProductData(ProductId("2"), "title 2", "product 2"),
    ProductData(ProductId("3"), "title 3", "product 3"),
    ProductData(ProductId("4"), "title 4", "product 4"),
    ProductData(ProductId("5"), "title 5", "product 5"),
    ProductData(ProductId("6"), "title 5", "product 5"),
    ProductData(ProductId("7"), "title 5", "product 5")
  )

  override def list()(
    implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace(s"list: ")
      postList
    }
  }

  override def get(id: ProductId)(
    implicit mc: MarkerContext): Future[Option[ProductData]] = {
    Future {
      logger.trace(s"get: id = $id")
      postList.find(post => post.id == id)
    }
  }

  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
