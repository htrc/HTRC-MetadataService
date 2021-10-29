package v2.controllers

import akka.stream.scaladsl.Source
import play.api.mvc._
import v2.dao.MetadataDao

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MetadataController @Inject()(metadataDao: MetadataDao,
                                   components: ControllerComponents)
                                  (implicit val ec: ExecutionContext) extends AbstractController(components) {

  def getMetadata(idsCsv: String): Action[AnyContent] =
    Action.async { implicit req =>
      render.async {
        case Accepts.Json() =>
          if (idsCsv == null || idsCsv.isEmpty)
            Future.successful(BadRequest)
          else {
            val ids = idsCsv.split("""\|""").toSet
            metadataDao.getMetadata(ids).map(publisher =>
              Ok.chunked(Source.fromPublisher(publisher))
            )
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
            metadataDao.getMetadata(ids).map(publisher =>
              Ok.chunked(Source.fromPublisher(publisher))
            )
          }
      }
    }
}
