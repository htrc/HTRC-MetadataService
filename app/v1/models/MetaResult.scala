package v1.models

import play.api.libs.json.{Json, Writes}

case class MetaResult(status: Int, found: Seq[VolMeta], missing: Seq[MetaError], invalid: Seq[String])

object MetaResult {
  implicit val resultWrites: Writes[MetaResult] = Json.writes[MetaResult]
}