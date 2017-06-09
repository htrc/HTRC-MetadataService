package v1.controllers

import java.io.FileInputStream
import javax.inject.Inject

import org.hathitrust.htrc.tools.pairtreehelper.PairtreeHelper
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import utils.Using._
import v1.models.{MetaError, MetaResult, VolMeta}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class MetadataController @Inject()(conf: Configuration)
                                  (implicit val ec: ExecutionContext) extends Controller {

  val pairtreeRoot: String = conf.getString("metadata-service.pairtree-root").get

  def getMetadata(idsCsv: String): Action[AnyContent] =
    Action.async { implicit req =>
      render.async {
        case Accepts.Json() =>
          if (idsCsv == null || idsCsv.isEmpty)
            Future.successful(BadRequest)
          else {
            collectMeta(idsCsv).map(mr => Ok(Json.toJson(mr)))
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
            collectMeta(req.body).map(mr => Ok(Json.toJson(mr)))
          }
      }
    }

  private def collectMeta(idsCsv: String): Future[MetaResult] = {
    val maybePairtreeDocs =
      Set(idsCsv.split("""\|""").map(_.trim): _*)
        .map(id => id -> Try(PairtreeHelper.getDocFromUncleanId(id)))

    val (pairtreeDocs, invalidDocs) = maybePairtreeDocs.partition(_._2.isSuccess)
    val invalidIds = invalidDocs.map { case (id, _) => id }.toSeq

    val maybeFutureMetadata =
      Future.sequence(
        pairtreeDocs
          .toSeq
          .collect {
            case (id, Success(doc)) =>
              id -> retrieveMeta(doc.getDocumentPathPrefix(pairtreeRoot) + ".json")
          }
          .map(futureToFutureTry)
      )

    maybeFutureMetadata.map(seq => {
      val (found, notFound) = seq.partition { case (_, result) => result.isSuccess }
      val foundMeta = found.collect { case (id, Success(json)) => VolMeta(id, json) }
      val missingMeta = notFound.collect { case (id, Failure(e)) => MetaError(id, e.getMessage) }

      var status = 0
      if (missingMeta.nonEmpty) status = 1
      if (invalidIds.nonEmpty) status |= 2

      MetaResult(status, foundMeta, missingMeta, invalidIds)
    })
  }

  private def retrieveMeta(path: String): Future[JsValue] = Future {
    using(new FileInputStream(path))(Json.parse)
  }

  def futureToFutureTry[T](pair: (String, Future[T])): Future[(String, Try[T])] = pair match {
    case (id, f) => f.map(id -> Success(_)).recover { case e => id -> Failure(e) }
  }
}
