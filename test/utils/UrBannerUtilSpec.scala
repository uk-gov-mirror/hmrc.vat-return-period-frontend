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

import base.BaseSpec
import play.api.i18n.Lang
import uk.gov.hmrc.hmrcfrontend.views.Aliases.{Cy, En, UserResearchBanner}

class UrBannerUtilSpec extends BaseSpec {

  "getUrBanner" when {

    "the showUserResearchBanner feature is disabled" should {

      "return None" in {
        mockAppConfig.features.showUserResearchBanner(false)
        UrBannerUtil.getUrBanner()(mockAppConfig, messages) shouldBe None
      }
    }

    "the showUserResearchBanner feature is enabled" when {

      "the language is English" should {

        "return a UserResearchBanner with En language and the English URL" in {
          mockAppConfig.features.showUserResearchBanner(true)
          UrBannerUtil.getUrBanner()(mockAppConfig, messages) shouldBe Some(UserResearchBanner(
            language = En,
            url = mockAppConfig.urBannerUrl("en"),
            hideCloseButton = false
          ))
        }
      }

      "the language is Welsh" should {

        "return a UserResearchBanner with Cy language and the Welsh URL" in {
          mockAppConfig.features.showUserResearchBanner(true)
          val welshMessages = messagesApi.preferred(Seq(Lang("cy")))
          UrBannerUtil.getUrBanner()(mockAppConfig, welshMessages) shouldBe Some(UserResearchBanner(
            language = Cy,
            url = mockAppConfig.urBannerUrl("cy"),
            hideCloseButton = false
          ))
        }
      }

      "hideCloseButton is true" should {

        "return a UserResearchBanner with hideCloseButton set to true" in {
          mockAppConfig.features.showUserResearchBanner(true)
          val result = UrBannerUtil.getUrBanner(hideCloseButton = true)(mockAppConfig, messages)
          result.map(_.hideCloseButton) shouldBe Some(true)
        }
      }
    }
  }
}