/*
 * Copyright 2016 HM Revenue & Customs
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

package common

import play.api.libs.json.Json

object GetSubscriptionResponses {

  //XZTAVC000187600
  val getSubFull = Json.parse(
    """
      |{
      |    "processingDate": "2001-12-17T09:30:47Z",
      |    "subscriptionType": {
      |        "safeId": "XA0000000012345",
      |        "correspondenceDetails": {
      |            "contactName": {
      |                "name1": "First",
      |                "name2": "Last"
      |            },
      |            "contactDetails": {
      |                "phoneNumber": "0000 10000",
      |                "mobileNumber": "0000 2000",
      |                "faxNumber": "0000 30000",
      |                "emailAddress": "test@test.com"
      |            },
      |            "contactAddress": {
      |                "addressLine1": "Line 1",
      |                "addressLine2": "Line 2",
      |                "addressLine3": "Line 3",
      |                "addressLine4": "Line 4",
      |                "countryCode": "GB",
      |                "postalCode": "AA1 1AA"
      |            }
      |        }
      |    }
      |}
    """.stripMargin
  )

  //XNTAVC000257565
  val getSubNoAddress = Json.parse(
    """
      |{
      |	"processingDate": "2001-12-17T09:30:47Z",
      |	"subscriptionType": {
      |		"safeId": "XA0000000012345",
      |		"correspondenceDetails": {
      |			"contactName": {
      |				"name1": "First",
      |				"name2": "Last"
      |			},
      |			"contactDetails": {
      |				"phoneNumber": "0000 10000",
      |				"mobileNumber": "0000 2000",
      |				"faxNumber": "0000 30000",
      |				"emailAddress": "test@test.com"
      |			}
      |		}
      |	}
      |}
    """.stripMargin
  )

  //XWTAVC000435628
  val getSubNoContactDetails = Json.parse(
    """
      |{
      |	"processingDate": "2001-12-17T09:30:47Z",
      |	"subscriptionType": {
      |		"safeId": "XA0000000012345",
      |		"correspondenceDetails": {
      |			"contactName": {
      |				"name1": "First",
      |				"name2": "Last"
      |			},
      |			"contactAddress": {
      |				"addressLine1": "Line 1",
      |				"addressLine2": "Line 2",
      |				"addressLine3": "Line 3",
      |				"addressLine4": "Line 4",
      |				"countryCode": "GB",
      |				"postalCode": "AA1 1AA"
      |			}
      |		}
      |	}
      |}
    """.stripMargin
  )

  //XBTAVC000739704
  val getSubMinForeignAddressWithDetails = Json.parse(
    """
     {
      |	"processingDate": "2001-12-17T09:30:47Z",
      |	"subscriptionType": {
      |		"safeId": "XA0000000012345",
      |		"correspondenceDetails": {
      |			"contactName": {
      |				"name1": "J"
      |			},
      |			"contactDetails": {
      |
      |			},
      |			"contactAddress": {
      |				"addressLine1": "Line 1",
      |				"addressLine2": "Line 2",
      |				"countryCode": "GG"
      |
      |			}
      |		}
      |	}
      |}
    """.stripMargin
  )

  //XATAVC000421817
  val getSubMinUkAddressWithDetails = Json.parse(

    """
      |{
      |	"processingDate": "2001-12-17T09:30:47Z",
      |	"subscriptionType": {
      |		"safeId": "XA0000000012345",
      |		"correspondenceDetails": {
      |			"contactName": {
      |				"name1": "J"
      |			},
      |			"contactDetails": {},
      |			"contactAddress": {
      |				"addressLine1": "Line 1",
      |				"addressLine2": "Line 2",
      |				"countryCode": "GB",
      |				"postalCode": "AA1 1AA"
      |			}
      |		}
      |	}
      |}
    """.stripMargin
  )

  //XWTAVC000616234
  val getSubMinimum = Json.parse(
    """
       {
      |	"processingDate": "2001-12-17T09:30:47Z",
      |	"subscriptionType": {
      |		"safeId": "XA0000000012345",
      |		"correspondenceDetails": {
      |			"contactName": {
      |				"name1": "J"
      |
      |			}
      |		}
      |	}
      |}
    """.stripMargin
  )

}
