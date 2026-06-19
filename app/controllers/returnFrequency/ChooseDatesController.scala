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

package controllers.returnFrequency

import audit.AuditService
import audit.models.StartJourneyAuditModel
import common.SessionKeys
import config.{AppConfig, ServiceErrorHandler}
import controllers.predicates.{AuthPredicate, InFlightAnnualAccountingPredicate, InFlightReturnFrequencyPredicate}
import forms.ChooseDatesForm.datesForm

import javax.inject.{Inject, Singleton}
import models.returnFrequency.{ReturnDatesModel, ReturnPeriod}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.returnFrequency.ChooseDates

import scala.concurrent.ExecutionContext

@Singleton
class ChooseDatesController @Inject()(authenticate: AuthPredicate,
                                      pendingReturnFrequency: InFlightReturnFrequencyPredicate,
                                      pendingAnnualAccountChange: InFlightAnnualAccountingPredicate,
                                      serviceErrorHandler: ServiceErrorHandler,
                                      mcc: MessagesControllerComponents,
                                      auditService: AuditService)
                                     (implicit appConfig: AppConfig,
                                      chooseDatesView: ChooseDates,
                                      ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {


  val show: Action[AnyContent] = (authenticate andThen pendingReturnFrequency andThen pendingAnnualAccountChange) { implicit user =>

    val currentReturnFrequency: String = user.session.get(SessionKeys.mtdVatvcCurrentReturnFrequency).get
    val form: Form[ReturnDatesModel] = user.session.get(SessionKeys.mtdVatvcNewReturnFrequency) match {
      case Some(value) => datesForm.fill(ReturnDatesModel(value))
      case _ => datesForm
    }

    ReturnPeriod(currentReturnFrequency).fold(serviceErrorHandler.showInternalServerError) { returnFrequency =>
      val auditModel = StartJourneyAuditModel(user, returnFrequency)
      auditService.extendedAudit(auditModel, Some(routes.ChooseDatesController.show.url))
      Ok(chooseDatesView(form, returnFrequency))
    }
  }

  val submit: Action[AnyContent] = (authenticate andThen pendingReturnFrequency andThen pendingAnnualAccountChange) { implicit user =>

    val currentReturnFrequency: String = user.session.get(SessionKeys.mtdVatvcCurrentReturnFrequency).get
    datesForm.bindFromRequest().fold(
      errors =>
        ReturnPeriod(currentReturnFrequency).fold(serviceErrorHandler.showInternalServerError)(returnFrequency =>
          BadRequest(chooseDatesView(errors, returnFrequency))
        ),
      success =>
        Redirect(controllers.returnFrequency.routes.ConfirmVatDatesController.show).addingToSession(SessionKeys.mtdVatvcNewReturnFrequency -> success.current)
    )
  }
}
