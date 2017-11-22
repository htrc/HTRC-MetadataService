package v2.dao

import com.google.inject.ImplementedBy
import v2.models.MetaResult

import scala.concurrent.Future

@ImplementedBy(classOf[MetadataMongoDaoImpl])
trait MetadataDao {

  def getMetadata(ids: Set[String]): Future[MetaResult]

}
