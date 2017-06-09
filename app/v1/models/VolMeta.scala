package v1.models

import play.api.libs.json.{JsValue, Json, Writes}

case class VolMeta(id: String, meta: JsValue)

object VolMeta {
  implicit val volMetaWrites: Writes[VolMeta] = Json.writes[VolMeta]
}