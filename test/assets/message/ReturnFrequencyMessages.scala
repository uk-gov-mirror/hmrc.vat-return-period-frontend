/*
 * Copyright 2024 HM Revenue & Customs
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

package assets.messages

object ReturnFrequencyMessages extends BaseMessages {

  val option1Jan = "January, April, July and October"
  val option2Feb = "February, May, August and November"
  val option3Mar = "March, June, September and December"
  val option4Monthly = "Monthly"
  val fullStop = "."
  val annually = "You currently submit returns annually. If you select a new VAT Return date, you will leave the Annual Accounting scheme."
  val changeEndOfMonth = "We’ll change your dates at the end of this month."
  val changeEndOfQuarter = "We’ll change your dates at the end of this quarter."

  object ChoosePage {
    val title: String = "What are the new VAT Return dates?" + titleSuffix
    val errorTitle: String =  "Error: What are the new VAT Return dates?" + titleSuffix
    val heading = "What are the new VAT Return dates?"
    val question = "The VAT Return dates are currently "
    val error = "Choose the new VAT Return dates"
  }

  object ConfirmPage {
    val title: String = "Check your answer" + titleSuffix
    val heading = "Check your answer"
    val heading2 = "VAT business details"
    val newDates = "VAT Return dates"
    val changeLink = "Change"
    val p2 = "By confirming this change, you agree that the information you have given is complete and correct."
    val annualAccountingOption = "If you choose to change your VAT Return dates you will:"
    val annualAccountingBullet1 = "leave the Annual Accounting scheme immediately"
    val annualAccountingBullet2 = "have to wait 12 months before you can rejoin the scheme"

  }

  object ReceivedPage {
    val title: String = "You have asked to change the VAT Return dates" + titleSuffix
    val titleAgent: String = "You have asked to change the VAT Return dates" + agentTitleSuffix
    val heading = "You have asked to change the VAT Return dates"
    val h2 = "What happens next"
    val oldChangeClientDetails = "You can change another client’s details."
    val backToClientDetails = "Back to client’s details"
    val p1Agent: String = "We will send an email to agentEmail@test.com within 2 working days telling you whether or not " +
      "the request has been accepted."
    val p2Agent = "We’ll contact MyCompany Ltd with an update."
    val p2AgentNoClientName = "We’ll contact your client with an update."
    val confirmationLetter: String = "We will send a confirmation letter to the agency address registered with HMRC within " +
      "15 working days."
    val digiPrefWithEmail: String = "We’ll send you an email within 2 working days with an update or you can check your HMRC secure messages."
    val digitalPref: String = "We will send you an email within 2 working days with an update, followed by a letter to " +
      "your principal place of business. You can also go to your HMRC secure messages to find out if your request has been accepted."
    val paperPref = "We will send a letter to your principal place of business with an update within 15 working days."
    val contactPrefError = "We will send you an update within 15 working days."
    val newDates = "The new VAT Return dates will only show on the account when they change. Until then, we will continue to show the current submission dates."
    val p1AgentBulk: String = "We’ll send an email to agentEmail@test.com within 2 working days telling you whether we can accept your request."

  }
}
