import api.cart.{CartRepository, CartRepositoryImpl}
import api.category.{CategoryRepository, CategoryRepositoryImpl}
import api.product.{ProductRepository, ProductRepositoryImpl}

import javax.inject._
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import v1.post._

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with ScalaModule {

  override def configure() = {
    bind[PostRepository].to[PostRepositoryImpl].in[Singleton]()
    bind[CartRepository].to[CartRepositoryImpl].in[Singleton]()
    bind[CategoryRepository].to[CategoryRepositoryImpl].in[Singleton]()
    bind[ProductRepository].to[ProductRepositoryImpl].in[Singleton]()
  }
}
