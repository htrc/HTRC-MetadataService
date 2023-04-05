package v2.controllers

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc._
import v2.dao.MetadataDao
import v2.models.MetaResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MetadataController @Inject()(metadataDao: MetadataDao,
                                   components: ControllerComponents)
                                  (implicit val ec: ExecutionContext, m: Materializer) extends AbstractController(components) {

  private val PlainText = Accepting(MimeTypes.TEXT)

  def getMetadata(idsCsv: String): Action[AnyContent] =
    Action.async { implicit req =>
      render.async {
        case Accepts.Json() =>
          if (idsCsv == null || idsCsv.isEmpty)
            Future.successful(BadRequest)
          else {
            val ids = idsCsv.split("""\|""").toSet
            metadataDao.getMetadata(ids)
              .flatMap(publisher => Source.fromPublisher(publisher).runWith(Sink.seq))
              .map { foundMeta =>
                val found = foundMeta
                  .map { meta =>
                    val id = meta("htid").as[String]
                    id -> meta
                  }
                  .toMap
                val notFoundIds = ids.diff(found.keySet)

                MetaResult(
                  found = Json.toJsObject(found),
                  missing = notFoundIds
                )
              }
              .map(result => Ok(Json.toJsObject(result)))
          }
      }
    }

  def getMultiMetadata: Action[String] =
    Action.async(parse.text) { implicit req =>
      render.async {
        case Accepts.Json() =>
          if (req.body == null || req.body.isEmpty)
            Future.successful(BadRequest)
          else {
            val ids = req.body.split("""[\|\n]""").toSet
            metadataDao.getMetadata(ids)
              .flatMap(publisher => Source.fromPublisher(publisher).runWith(Sink.seq))
              .map { foundMeta =>
                val found = foundMeta
                  .map { meta =>
                    val id = meta("htid").as[String]
                    id -> meta
                  }
                  .toMap
                val notFoundIds = ids.diff(found.keySet)

                MetaResult(
                  found = Json.toJsObject(found),
                  missing = notFoundIds
                )
              }
              .map(result => Ok(Json.toJsObject(result)))
          }
      }
    }

  def streamMetadata(idsCsv: String): Action[AnyContent] =
    Action.async { implicit req =>
      render.async {
        case PlainText() =>
          if (idsCsv == null || idsCsv.isEmpty)
            Future.successful(BadRequest)
          else {
            val ids = idsCsv.split("""\|""").toSet
            metadataDao.getMetadata(ids).map(publisher =>
              Ok.chunked(Source.fromPublisher(publisher).map(_.toString).intersperse(System.lineSeparator))
            )
          }
      }
    }

  def streamMultiMetadata: Action[String] =
    Action.async(parse.text) { implicit req =>
      render.async {
        case PlainText() =>
          if (req.body == null || req.body.isEmpty)
            Future.successful(BadRequest)
          else {
            val ids = req.body.split("""[\|\n]""").toSet
            metadataDao.getMetadata(ids).map(publisher =>
              Ok.chunked(Source.fromPublisher(publisher).map(_.toString).intersperse(System.lineSeparator))
            )
          }
      }
    }
}
