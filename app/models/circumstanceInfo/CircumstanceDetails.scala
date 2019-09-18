/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.circumstanceInfo

import models.core.JsonReadUtil
import models.returnFrequency.ReturnPeriod
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, Writes, __}

case class CircumstanceDetails(customerDetails: CustomerDetails,
                               returnPeriod: Option[ReturnPeriod],
                               changeIndicators: Option[ChangeIndicators],
                               partyType: Option[String])

object CircumstanceDetails extends JsonReadUtil {

  private val customerDetailsPath = __ \ "customerDetails"
  private val returnPeriodPath = __ \ "returnPeriod"
  private val changeIndicatorsPath = __ \ "changeIndicators"
  private val partyTypePath = __ \ "partyType"

  implicit val reads: Reads[CircumstanceDetails] = (
    customerDetailsPath.read[CustomerDetails] and
      returnPeriodPath.readOpt[ReturnPeriod] and
      changeIndicatorsPath.readOpt[ChangeIndicators] and
      partyTypePath.readOpt[String]
    ) (CircumstanceDetails.apply _)

  implicit val writes: Writes[CircumstanceDetails] = (
    customerDetailsPath.write[CustomerDetails] and
      returnPeriodPath.writeNullable[ReturnPeriod] and
      changeIndicatorsPath.writeNullable[ChangeIndicators] and
      partyTypePath.writeNullable[String]
    ) (unlift(CircumstanceDetails.unapply))

}
