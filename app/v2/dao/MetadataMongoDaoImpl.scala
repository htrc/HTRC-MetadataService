package v2.dao

import javax.inject.{Inject, Singleton}

import org.reactivestreams.Publisher
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.DefaultDB
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MetadataMongoDaoImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)
                                    (implicit ec: ExecutionContext) extends MetadataDao {
  def db: Future[DefaultDB] = reactiveMongoApi.database

  private def metaColl: Future[JSONCollection] = db.map(_.collection[JSONCollection]("metadata"))

  override def getMetadata(ids: Set[String]): Future[Publisher[JsObject]] = {
    val query = Json.obj(
      "volumeId" -> Json.obj(
        "$in" -> ids
      )
    )
    val projection = Json.obj(
      "_id" -> 0
    )

    metaColl
      .map(_
        .find(query, projection)
        .cursor[JsObject]()
        .enumerator()
      )
      .map(IterateeStreams.enumeratorToPublisher(_))
  }
}
