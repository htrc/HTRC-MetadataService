package v2.dao

import akka.stream.Materializer
import org.reactivestreams.Publisher
import play.api.libs.json._

import javax.inject.{Inject, Singleton}

// Reactive Mongo imports
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.bson.collection.BSONCollection

// BSON-JSON conversions/collection
import reactivemongo.akkastream.cursorProducer
import reactivemongo.play.json.compat._
import reactivemongo.play.json.compat.json2bson.{toDocumentReader, toDocumentWriter}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MetadataMongoDaoImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                                    (implicit ec: ExecutionContext, m: Materializer)
  extends MetadataDao with ReactiveMongoComponents {

  //val _ = implicitly[reactivemongo.api.bson.BSONDocumentWriter[JsObject]]

  private def metaColl: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("metadata"))

  override def getMetadata(ids: Set[String]): Future[Publisher[JsObject]] = {
    val query = Json.obj(
      "htid" -> Json.obj(
        "$in" -> ids
      )
    )
    val projection = Json.obj(
      "_id" -> 0
    )

    metaColl
      .map(_
        .find(query, Some(projection))
        .cursor[JsObject]()
        .documentPublisher()
      )
  }
}
