package v1.models

import play.api.libs.json.{Json, Writes}

case class MetaError(id: String, error: String)

object MetaError {
  implicit val metaErrorWrites: Writes[MetaError] = Json.writes[MetaError]
}