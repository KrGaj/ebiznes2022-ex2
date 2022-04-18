package api.category

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying post information.
  */
case class CategoryResource(id: String, link: String, title: String, body: String)

object CategoryResource {
  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
  implicit val format: Format[CategoryResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[CategoryResource]]
  */
class CategoryResourceHandler @Inject()(
    routerProvider: Provider[CategoryRouter],
    categoryRepository: CategoryRepository)(implicit ec: ExecutionContext) {

  def create(postInput: CategoryFormInput)(
    implicit mc: MarkerContext): Future[CategoryResource] = {
    val data = CategoryData(CategoryId("999"), postInput.title, postInput.body)
    // We don't actually create the post, so return what we have
    categoryRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(
    implicit mc: MarkerContext): Future[Option[CategoryResource]] = {
    val categoryFuture = categoryRepository.get(CategoryId(id))
    categoryFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[CategoryResource]] = {
    categoryRepository.list().map { postDataList =>
      postDataList.map(postData => createPostResource(postData))
    }
  }

  private def createPostResource(p: CategoryData): CategoryResource = {
    CategoryResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}

