package v2.controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc._
import v2.dao.MetadataDao

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
            metadataDao.getMetadata(ids).map(result => Ok(Json.toJsObject(result)))
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
            val ids = req.body.split("""\|""").toSet
            metadataDao.getMetadata(ids).map(result => Ok(Json.toJsObject(result)))
          }
      }
    }
}
