package v2.dao

import com.google.inject.ImplementedBy
import org.reactivestreams.Publisher
import play.api.libs.json.JsObject

import scala.concurrent.Future

@ImplementedBy(classOf[MetadataMongoDaoImpl])
trait MetadataDao {

  def getMetadata(ids: Set[String]): Future[Publisher[JsObject]]

}
