package v2.models

import play.api.libs.json.{JsObject, Json, OWrites}

case class MetaResult(found: JsObject, missing: Set[String])

object MetaResult {
  implicit val resultWrites: OWrites[MetaResult] = Json.writes[MetaResult]
}