package api.cart

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import api.product.ProductId
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class CartData(id: CartId, title: String, body: String, productId: ProductId)

class CartId private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object CartId {
  def apply(raw: String): CartId = {
    require(raw != null)
    new CartId(Integer.parseInt(raw))
  }
}

class CartExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait CartRepository {
  def create(data: CartData)(implicit mc: MarkerContext): Future[CartId]

  def list()(implicit mc: MarkerContext): Future[Iterable[CartData]]

  def get(id: CartId)(implicit mc: MarkerContext): Future[Option[CartData]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class CartRepositoryImpl @Inject()()(implicit ec: CartExecutionContext)
  extends CartRepository {

  private val logger = Logger(this.getClass)

  private val CartList = List(
    CartData(CartId("1"), "name 1", "cart item 1", ProductId("2")),
    CartData(CartId("2"), "name 2", "cart item 2", ProductId("1")),
    CartData(CartId("3"), "name 3", "cart item 3", ProductId("3")),
    CartData(CartId("4"), "name 4", "cart item 4", ProductId("7")),
  )

  override def list()(
    implicit mc: MarkerContext): Future[Iterable[CartData]] = {
    Future {
      logger.trace(s"list: ")
      CartList
    }
  }

  override def get(id: CartId)(
    implicit mc: MarkerContext): Future[Option[CartData]] = {
    Future {
      logger.trace(s"get: id = $id")
      CartList.find(post => post.id == id)
    }
  }

  def create(data: CartData)(implicit mc: MarkerContext): Future[CartId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}

