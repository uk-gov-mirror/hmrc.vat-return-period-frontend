/*
 * Copyright 2026 HM Revenue & Customs
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

package utils

import config.AppConfig
import play.api.i18n.Messages
import uk.gov.hmrc.hmrcfrontend.views.Aliases.{Cy, En, UserResearchBanner}

object UrBannerUtil {

  def getUrBanner(hideCloseButton:Boolean = false)(implicit appConfig:AppConfig, messages: Messages):Option[UserResearchBanner] =
    Option.when(appConfig.features.showUserResearchBanner())(UserResearchBanner(
      language = if(messages.lang.code == "en") En else Cy,
      url = appConfig.urBannerUrl(messages.lang.code),
      hideCloseButton = hideCloseButton
    ))

}
