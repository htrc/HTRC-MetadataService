package v2.dao

import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import v2.models.MetaResult

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MetadataMongoDaoImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)
                                    (implicit ec: ExecutionContext) extends MetadataDao {
  def db: Future[DefaultDB] = reactiveMongoApi.database

  private def metaColl: Future[JSONCollection] = db.map(_.collection[JSONCollection]("metadata"))

  override def getMetadata(ids: Set[String]): Future[MetaResult] = {
    val errorHandler = Cursor.FailOnError[Set[JsObject]]()

    metaColl
      .flatMap(_
        .find(
          Json.obj(
            "volumeId" -> Json.obj(
              "$in" -> ids
            )
          ),
          Json.obj(
            "_id" -> 0
          )
        )
        .cursor[JsObject]()
        .collect[Set](Int.MaxValue, errorHandler)
        .map { foundMeta =>
          val found = foundMeta
            .map { meta =>
              val id = meta("volumeId").as[String]
              id -> meta
            }
            .toMap
          val notFoundIds = ids.diff(found.keySet)

          MetaResult(
            found = Json.toJsObject(found),
            missing = notFoundIds
          )
        }
      )
  }
}
