# API Specification

## POST /v1/payments

This endpoint creates a new payment.

### Request example

```
POST /v1/payments
Authorization: Bearer BEARER_TOKEN
Content-Type: application/json

{
    "amount": 50000,
    "description": "Payment description",
    "return_url": "https://service.example.com/some-reference-to-this-payment",
    "reference" : "some-reference-to-this-payment"
}
```
### Request example with optional fields
#### Note: these optional fields are only valid for card payments!
```
POST /v1/payments
Authorization: Bearer BEARER_TOKEN
Content-Type: application/json

{
    "amount": 50000,
    "description": "Payment description",
    "return_url": "https://service.example.com/some-reference-to-this-payment",
    "reference" : "some-reference-to-this-payment",
    "email": "foo@example.org",
    "prefilled_cardholder_details": {
     	"cardholder_name": "J Foo",
        "billing_address": {
              "line1": "address line 1",
              "line2": "address line 2",
              "postcode": "AB1 CD2",
              "city": "address city",
              "country": "GB"
        }
     }
}
```

#### Request description

BEARER_TOKEN: A valid bearer token for the account to associate the payment with.

| Field                    | required | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `amount`                 | Yes      | Amount to pay in pence                           |
| `description`            | Yes      | Payment description                              |
| `return_url`             | Yes      | The URL where the user should be redirected to when the payment workflow is finished (**must be HTTPS only**).         |
| `reference`              | Yes      | There reference issued by the government service for this payment         |
| `email`                  | No       | Email address of the payer |
| `cardholder_name`        | No       | Name of the payer |
| `line1`                  | No       | Line 1 of the payer's address |
| `line2`                  | No       | Line 2 of the payer's address |
| `postcode`               | No       | Postcode of the payer's address |
| `city`                   | No       | City of the payer's address |
| `country`                | No       | ISO 3166-1 country code of the payer's address |

### Payment created response
In case of a card payment:

```
HTTP/1.1 201 Created
Location: https://publicapi.example.com/v1/payments/ab2341da231434
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434",
            "method": "GET" 
        },
        "next_url" : {
            "href": "https://frontend.example.com/charge/1?chargeTokenId=82347",
            "method": "GET" 
        },
        "next_url_post" : {
            "params" : {
                "chargeTokenId" : "82347"
            },
            "type" : "application/x-www-form-urlencoded",
            "href": "https://frontend.example.com/charge/1?chargeTokenId=82347",
            "method": "POST" 
        },
        "events" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434/events",
            "method": "GET" 
        },
        "refunds" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434/refunds",
            "method": "GET" 
        },
        "cancel" : {
            "params" : {},
            "type" : "",
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434/cancel",
            "method": "POST" 
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://service.example.com/some-reference-to-this-payment",
    "reference": "some-reference-to-this-payment",
    "payment_provider": "Sandbox",
    "card_brand": "Mastercard",
    "created_date": "2016-01-15T16:30:56Z",
    "email": "foo@example.org",
    "card_details": {
        "last_digits_card_number": null,
        "first_digits_card_number": null,
        "cardholder_name": "J Foo",
        "expiry_date": null,
        "billing_address": {
            "line1": "address line 1",
            "line2": "address line 2",
            "postcode": "AB1 CD2",
            "city": "address city",
            "country": "GB"
        },
        "card_brand": ""
    },
    "refund_summary": {
        "status": "available"
        "amount_available": 14500
        "amount_submitted": 0
    }
    "settlement_summary": {
        "captured_date": "2016-01-15",
        "capture_submit_time": "2016-01-15T16:30:56Z" 
    }
}
```

  
In case of a direct debit payment:

```
HTTP/1.1 201 Created
Location: https://publicapi.example.com/v1/payments/ab2341da231434
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434",
            "method": "GET" 
        },
        "next_url" : {
            "href": "https://frontend.example.com/charge/1?chargeTokenId=82347",
            "method": "GET" 
        },
        "next_url_post" : {
            "params" : {
                "chargeTokenId" : "82347"
            },
            "type" : "application/x-www-form-urlencoded",
            "href": "https://frontend.example.com/charge/1?chargeTokenId=82347",
            "method": "POST" 
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://service.example.com/some-reference-to-this-payment",
    "reference": "some-reference-to-this-payment",
    "payment_provider": "Sandbox",
    "created_date": "2016-01-15T16:30:56Z",
}
```
#### Response fields description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `payment_id`           | The ID of the created payment             |
| `amount`               | Amount to pay in pence                    |
| `description`          | Payment description                       |
| `status`               | Current status of the payment             |
| `return_url`           | The URL where the user should be redirected to when the payment workflow is finished.    |
| `reference`            | The reference issued by the government service for this payment                          |
| `payment_provider`     | The payment provider for this payment                                                    |
| `card_brand`           | The card brand used for this payment                                                     |
| `created_date`         | The payment creation date for this payment                                               |
| `refund_summary.status`| Refund availability status of the payment                                                |
| `refund_summary.amount_available`| Amount available for refunds                                                   |
| `refund_summary.amount_submitted`| Total amount of refunds submitted for this payment                             |
| `settlement_summary.captured_date`| Date of the capture according to the payment gateway                          |
| `settlement_summary.capture_submit_time`| Date and time of submission of the capture request, if present          |
| `created_date`         | The payment creation date for this payment                                               |
| `_links.self`          | Link to the payment                                                                      |
| `_links.next_url`      | Where to navigate the user next as a GET                                                 |
| `_links.next_url_post` | Where to navigate the user next as a POST                                                |
| `_links.events`        | Link to payment events                                                |
| `_links.refunds`       | Link to payment refunds                                                |
| `_links.cancel`        | Link to cancel the payment (link only available when a payment can be cancelled (i.e. payment has one of the statuses - CREATED, IN PROGRESS |

### Payment creation response errors

#### Unrecognised response from Connector
Payment creation is now very defensive and with these validations in place Connector receives a valid Create payment request, so failing to
create a payment should be very rare (infrastructure failures for example).

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 34

{
    "code": "P0198",
    "description": "Downstream system error"
}
```

#### Validation errors
Payment request contains a valid Json payload with expected fields but it contains validation errors.

```
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/json
Content-Length: 34

{
    "field: "amount",
    "code": "P0102",
    "description": "Invalid attribute value: amount. Must be greater than or equal to 1"
}
```

#### Missing, null or empty mandatory field
Payment request contains a valid Json payload but it has a missing (includes null or empty) field

```
HTTP/1.1 400 Bad Request
Content-Type: application/json
Content-Length: 34

{
    "field: "reference",
    "code": "P0101",
    "description": "Missing mandatory attribute: reference"
}
```

#### Unable to parse request body
Payment creation request is malformed, so it can't be processed

```
HTTP/1.1 400 Bad Request
Content-Type: application/json
Content-Length: 34

{
    "code": "P0100",
    "description": "Unable to parse JSON"
}
```

#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `field`            | Field related to the error (Only for validation or missing fields errors) |
| `code`             | The error reference. Format: P01XX                                        |
| `description`      | The error description                                                     |

#### Response error codes

| Code               | Description                                                       |
| ------------------ | ------------------------------------------------------------------|
| `P0199`            | Auth token was correct but the account wasn't found in Connector  |
| `P0198`            | Connector response was unrecognised to PublicAPI                  |
| `P0100`            | Body sent by the client can't be processed (is not a valid JSON)  |
| `P0101`            | An mandatory attribute in the JSON body is missing, null or empty |
| `P0102`            | An attribute in the JSON body has a validation error              |

------------------------------------------------------------------------------------------------

## GET /v1/payments/{paymentId}

Returns a payment by ID.

### Request example

```
GET /v1/payments/ab2341da231434
Authorization: Bearer BEARER_TOKEN
```

### Payment response
In case of a card payment:

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434",
            "method": "GET" 
        },
        "next_url" : {
            "href": "https://frontend.example.com/charge/ab2341da231434?chargeTokenId=82347",
            "method": "GET" 
        },
        "next_url_post" : {
            "params" : {},
            "type" : "",
            "href": "https://frontend.example.com/charge/1?chargeTokenId=82347",
            "method": "POST" 
        },
        "events" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434/events",
            "method": "GET" 
        },
        "refunds" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434/refunds",
            "method": "GET" 
        },
        "cancel" : {
            "params" : {},
            "type" : "",
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434/cancel",
            "method": "POST"
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "fee": 10,
    "net_amount": 49995
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://service.example.com/some-reference-to-this-payment",
    "reference" : "some-reference-to-this-payment",
    "email": "mail@example.com",
    "payment_provider": "Sandbox",
    "card_brand": "Visa",
    "created_date": "2016-01-15T16:30:56Z",
    "card_details": {  
    	"last_digits_card_number":"1234",
    	"first_digits_card_number":"123456",
    	"cardholder_name":"Mr. Payment",
    	"expiry_date":"12/19",
    	"card_brand":"Mastercard"
    	"billing_address":{  
    	   "line1":"line1",
    	   "line2":"line2",
    	   "postcode":"AB2 DEF",
    	   "city":"city",
    	   "country":"UK"
    	}
    },
    "refund_summary": {
        "status": "available"
        "amount_available": 14500
        "amount_submitted": 0
    },
    "settlement_summary": {
        "captured_date": "2016-01-15",
        "capture_submit_time": "2016-01-15T16:30:56Z" 
    }
}
```

In case of a direct debit payment:

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434",
            "method": "GET" 
        },
        "next_url" : {
            "href": "https://frontend.example.com/secure/ab2341da231434",
            "method": "GET" 
        },
        "next_url_post" : {
            "params" : {},
            "type" : "",
            "href": "https://frontend.example.com/secure/ab2341da231434",
            "method": "POST" 
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://service.example.com/some-reference-to-this-payment",
    "reference" : "some-reference-to-this-payment",
    "email": "mail@example.com",
    "payment_provider": "Sandbox",
    "created_date": "2016-01-15T16:30:56Z"
}
```

#### Response field description

See: [Payment created](#payment-created-response)

### GET Payment response errors

#### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0200"
    "description": "Not found"
}
```

#### Unrecognised response from Connector

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
    "code" : "P0298"
    "description": "Downstream system error"
}
```

#### Response errors field description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P02XX                                        |
| `description`      | The error description                                                     |

#### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0200`            | Connector response was 404 Not Found             |
| `P0298`            | Connector response was unrecognised to PublicAPI |

------------------------------------------------------------------------------------------------

## GET /v1/payments/{paymentId}/events

Returns the list of events associated with a payment.

### Request example

```
GET /v1/payments/ab2341da231434/events
Authorization: Bearer BEARER_TOKEN
```

### Payment events response

```
HTTP/1.1 200 OK
Content-Type: application/json

"events": [
        {
            "payment_id": "ab2341da231434",
            "status": "CREATED",
            "updated": "2016-01-13 17:42:16",
            "_links": {
                "payment_url" : {
                    "method": "GET",
                    "href": "https://publicapi.example.com/v1/payments/ab2341da231434"
                }
            }
        },
        {
            "payment_id": "ab2341da231434",
            "status": "IN PROGRESS",
            "updated": "2016-01-13 17:42:28",
            "_links": {
                "payment_url" : {
                    "method": "GET",
                    "href": "https://publicapi.example.com/v1/payments/ab2341da231434"
                }
            }
        },
        {
            "payment_id": "ab2341da231434",
            "status": "SUCCEEDED",
            "updated": "2016-01-13 17:42:29",
            "_links": {
                "payment_url" : {
                    "method": "GET",
                    "href": "https://publicapi.example.com/v1/payments/ab2341da231434"
                }
            }
        }
    ],
    "_links": {
        "self" :{
            "href": "https://publicapi.example.com/v1/payments/ab2341da231434",
            "method": "GET" 
        }           
    },
    "payment_id": "ab2341da231434"

```

#### Response field description

See: [Payment created](#payment-created-response)

### GET Payment Events response errors

#### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0300"
    "description": "Not found"
}
```

#### Unrecognised response from Connector

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
    "code" : "P0398"
    "description": "Downstream system error"
}
```

#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |

#### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0300`            | Connector response was 404 Not Found             |
| `P0398`            | Connector response was unrecognised to PublicAPI |

------------------------------------------------------------------------------------------------
## GET /v1/payments/{paymentId}/refunds

Returns a list of refunds associated with a payment.

### Request example

```
GET /v1/payments/ab2341da231434/refunds
Authorization: Bearer BEARER_TOKEN
```

### Payment refunds response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "payment_id": "ab2341da231434",
  "_links": {
    "self": {
      "href": "https://publicapi.example.com/v1/payments/ab2341da231434/refunds"
    },
    "payment": {
      "href": "https://publicapi.example.com/v1/payments/ab2341da231434"
    }
  },
  "_embedded": {
    "refunds": [
      {
  	    "_links": {
          "self" :{
            "href": "https://publicapi.example.com/v1/payments/abc123/refunds/xyz123",
          },
         "payment" :{
           "href": "https://publicapi.example.com/v1/payments/abc123",
         }
       }
       "refund_id": "xyz123",
       "amount": 25000,
       "status": "submitted",
       "created_date": "2016-06-01 16:34:23",
      },
      {
        "_links": {
            "self" :{
                "href": "https://publicapi.example.com/v1/payments/abc123/refunds/xyz124"
            },
            "payment" :{
                "href": "https://publicapi.example.com/v1/payments/abc123"              
            }
          }
          "refund_id": "xyz124",
          "amount": 22000,
          "status": "success",
          "created_date": "2016-06-01 16:34:23",
    ]
  }
}
```

#### Response field description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `payment_id`           | The ID of the created payment             |
| `_embedded.refunds.refund_id`               | The ID of this refund                    |
| `_embedded.refunds.amount`          | The amount of refund                       |
| `_embedded.refunds.status`               | Current status of the refund (submitted/success)             |
| `_embedded.refunds.created_date`           | Date when the refund was created    |

### GET Payment Refunds response errors

#### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0800"
    "description": "Not found"
}
```
#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |

------------------------------------------------------------------------------------------------
## GET /v1/payments/{paymentId}/refunds/{refundId}

Returns the refund by ID for the payment.

### Request example

```
GET /v1/payments/ab2341da231434/refunds/xyz124
Authorization: Bearer BEARER_TOKEN
```

### Payment refund response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "amount": 1
    "created_date": "2016-08-10T12:31:45.802Z"
    "refund_id": "548g68390f2pbu1po9eifdqhaq"
    "status": "submitted"
    "_links": {
        "self": {
            "href": "https://publicapi.example.com/v1/payments/abc123/refunds/xyz124"
        }
        "payment": {
            "href": "https://publicapi.example.com/v1/payments/abc123"
        }
    }
}
```

#### Response field description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `refund_id`            | The ID of the refund                      |
| `amount`               | Amount in pence for the refund            |
| `status`               | Current status of the refund (submitted/success)             |
| `created_date`         | Date when the refund was created          |
| `_links.self`          | Self link for this refund                 |
| `_links.payment`       | Link for the payment of this refund       |

### GET Payment Refunds response errors

#### Refund ID not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0700"
    "description": "Not found"
}
```
#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |


------------------------------------------------------------------------------------------------
## POST /v1/payments/{paymentId}/refunds

Creates a new refund associated with the payment.

### Request example

```
POST /v1/payments/ab2341da231434/refunds
Authorization: Bearer BEARER_TOKEN
{
    "amount": 25000,
    "refund_amount_available": 30000
}
```

#### Request description

BEARER_TOKEN: A valid bearer token for the account to associate the payment with.

| Field                    | required | Description                                             |
| ------------------------ |:--------:| ------------------------------------------------------- |
| `amount`                 | Yes      | Amount to refund in pence                               |
| `refund_amount_available`| No       | Total amount still available before issuing the refund  |

### Refund created response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "https://publicapi.example.com/v1/payments/abc123/refunds/xyz123",

        },

        "payment" :{
            "href": "https://publicapi.example.com/v1/payments/abc123",

        },
    }
    "refund_id": "x2aysfsg3a3s45z123",
    "amount": 25000,
    "status": "submitted",
    "created_date": "2016-06-01 16:34:23",
}
```

#### Response fields description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `refund_id`            | The ID of the refund created             |
| `amount`               | Amount of refund in pence                |
| `status`               | Current status of the refund             |
| `created_date`         | The creation date for this refund        |
| `_links.self`          | Link to this refund                      |
| `_links.payment`       | Link to the payment this refund relates to|

#### Refund amount available mismatch

```
HTTP/1.1 412 Precondition Failed
Content-Type: application/json

{
    "code" : "P0604"
    "description": "Refund amount available mismatch"
}
```

### POST Payment Refunds response errors

#### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0600"
    "description": "Not found"
}
```
#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |
------------------------------------------------------------------------------------------------
## POST /v1/payments/{paymentId}/cancel

This endpoint cancels a new payment. A payment can only be cancelled if it's state is one of the following (case-insensitive):

| Cancellable payment states |
| -------------------------- |
| Created                    |
| In Progress                |


### Request example

```
POST /v1/payments/ab2341da231434/cancel
Authorization: Bearer BEARER_TOKEN
```

### Payment cancellation successful

```
HTTP/1.1 204 No Content
```

### Payment cancellation response errors

#### Payment not found

The payment state is not cancellable.

```
HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 44

{
    "code" : "P0500"
    "description" : "Not found"
}
```

#### Payment cancellation failed

The payment state is not cancellable.

```
HTTP/1.1 400 Bad Request
Content-Type: application/json
Content-Length: 44

{
    "code" : "P0501"
    "description" : "Cancellation of charge failed"
}
```

#### Unrecognised response from Connector

The payment state is not cancellable.

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 44

{
    "code" : "P0598"
    "description" : "Downstream system error"
}
```

#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P05XX                                        |
| `description`      | The error description                                                     |


#### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0500`            | Connector response was 404 Not Found             |
| `P0501`            | Connector response was 400 Bad Request           |
| `P0598`            | Connector response was unrecognised to PublicAPI |

------------------------------------------------------------------------------------------------
## GET /v1/payments

This endpoint searches for transactions for the given account id, with filters and pagination

### Request example

```
GET /v1/payments

```

#### Query Parameters description

| Field           | required | Description                               |
| --------------- |:--------:| ----------------------------------------- |
| `reference`     |    -     | There (partial or full) reference issued by the government service for this payment. |
| `state`         |    -     | The state of this payment. See notes below |
| `from_date`     |    -     | The initial date for search payments |
| `to_date`       |    -     | The end date for search payments |
| `card_brand`    |    -     | The card brand for search payments. For Card Payments only. If used for Direct Debit payment a BadRequestException is thrown  |
| `page`          |    -     | To get the results from the specified page number, should be a non zero +ve number (optional, defaults to 1)|
| `display_size`  |    -     | Number of records to be returned per page, should be a non zero +ve number (optional, defaults to 500)|
| `email`         |    -     | Email ID of the payment user to search for          |
| `agreement_id`  |    -     | Agreement id. Used by Direct Debit exclusively. If used for Card payment a BadRequestException is thrown. |
| `first_digits_card_number`  |    -     | First six (6) digits of the card used to make payment. If used for Direct Debit payment a BadRequestException is thrown. |
| `last_digits_card_number`  |    -     | Last four (4) digits of the card used to make payment. If used for Direct Debit payment a BadRequestException is thrown. |
| `cardholder_name`  |    -     | Name on card used to make payment. If used for Direct Debit payment a BadRequestException is thrown. |
| `agreement_id`  |    -     | Agreement id. Used by Direct Debit exclusively. If used for Card payment a BadRequestException is thrown. |

#### Notes:
`Valid states for Card Payments: "created", "started", "submitted", "success", "failed", "cancelled", "error"`

`Valid states for Direct Debit Payments: "started", "pending", "success", "failed", "cancelled"`

### Response example for Card Payment

```
{
  "total": 5,
  "count": 2,
  "page": 2,
  "results": [
    {
      "payment_id": "4hn0c8bbtfbnp5tmite2274h5c",
      "payment_provider": "sandbox",
      "card_brand": "",
      "amount": 1,
      "state": {
        "status": "started",
        "finished": false
      },
      "description": "desc",
      "return_url": "https://demoservice.example.com//return/rahul-ref",
      "reference": "rahul-ref",
      "email": "mail@example.com",
      "created_date": "2016-05-23T15:22:50.972Z",
      "card_details": {  
      	"last_digits_card_number":"1234",
      	"first_digits_card_number":"123456",
      	"cardholder_name":"Mr. Payment",
      	"expiry_date":"12/19",
      	"card_brand":"Mastercard"
      	"billing_address":{  
      	   "line1":"line1",
      	   "line2":"line2",
      	   "postcode":"ABC2 DEF",
      	   "city":"city",
      	   "country":"UK"
      	}
      },
      "refund_summary": {
         "status": "pending"
         "amount_available": 1
         "amount_submitted": 0
      },
      "settlement_summary": {
          "captured_date": "2016-01-15",
          "capture_submit_time": "2016-01-15T16:30:56Z" 
      },
      "_links": {
        "self": {
          "href": "https://publicapi.example.com/v1/payments/4hn0c8bbtfbnp5tmite2274h5c",
          "method": "GET"
        },
        "cancel": {
          "href": "https://publicapi.example.com/v1/payments/4hn0c8bbtfbnp5tmite2274h5c/cancel",
          "method": "POST"
        },
        "events": {
          "href": "https://publicapi.example.com/v1/payments/4hn0c8bbtfbnp5tmite2274h5c/events",
          "method": "GET"
        },
        "refunds": {
          "href": "https://publicapi.example.com/v1/payments/br0ih3laeacuf3845j4q77d11p/refunds"
          "method": "GET"
        }
      }
    },
    {
      "payment_id": "am6f5d1583563deb7ss5obju2",
      "payment_provider": "sandbox",
      "card_brand": "",
      "amount": 1,
      "state": {
        "status": "started",
        "finished": false
      },
      "description": "desc",
      "return_url": "https://demoservice.example.com/return/rahul-ref",
      "reference": "rahul-ref",
      "email": "mail@example.com",
      "created_date": "2016-05-23T15:22:47.038Z",
      "card_details": {  
      	"last_digits_card_number":"1234",
      	"first_digits_card_number":"123456",
      	"cardholder_name":"Mrs. Payment",
      	"expiry_date":"12/19",
      	"card_brand":"Mastercard"
      	"billing_address":{  
      	   "line1":"line1",
      	   "line2":"line2",
      	   "postcode":"IJK3 LMN",
      	   "city":"city",
      	   "country":"UK"
      	}
      },
      "refund_summary": {
         "status": "pending"
         "amount_available": 1
         "amount_submitted": 0
      },
      "settlement_summary": {
         "captured_date": "2016-01-15",
         "capture_submit_time": "2016-01-15T16:30:56Z" 
        },
      "_links": {
        "self": {
          "href": "https://publicapi.example.com/v1/payments/am6f5d1583563deb7ss5obju2",
          "method": "GET"
        },
        "cancel": {
          "href": "https://publicapi.example.com/v1/payments/am6f5d1583563deb7ss5obju2/cancel",
          "method": "POST"
        },
        "events": {
          "href": "https://publicapi.example.com/v1/payments/am6f5d1583563deb7ss5obju2/events",
          "method": "GET"
        }
        "refunds": {
          "href": "https://publicapi.example.com/v1/payments/br0ih3laeacuf3845j4q77d11p/refunds"
          "method": "GET"
        }
      }
    }
  ],
  "_links": {
    "next_page": {
      "href": "https://publicapi.example.com/v1/payments?page=3&display_size=2"
    },
    "self": {
      "href": "https://publicapi.example.com/v1/payments?page=2&display_size=2"
    },
    "prev_page": {
      "href": "https://publicapi.example.com/v1/payments?page=1&display_size=2"
    },
    "last_page": {
      "href": "https://publicapi.example.com/v1/payments?page=3&display_size=2"
    },
    "first_page": {
      "href": "https://publicapi.example.com/v1/payments?page=1&display_size=2"
    }
  }
}
```

#### Response field description for Card Payment

| Field                             | Always present | Description                                                       |
| ------------------------          |:--------------:| ----------------------------------------------------------------- |
| `total`                           | Yes            | Total number of payments found                                    |
| `count`                           | Yes            | Number of payments displayed on this page                         |
| `page`                            | Yes            | Page number of the current recordset                              |
| `results`                         | Yes            | List of payments                                                  |
| `results[i].payment_id`              | Yes            | The unique identifier for this payment                            |
| `results[i].amount`                  | Yes            | The amount of this payment in pence                               |
| `results[i].card_brand`              | Yes            | The card brand used for this payment                              |
| `results[i].description`             | Yes            | The payment description                                           |
| `results[i].reference`               | Yes            | There reference issued by the government service for this payment |
| `results[i].email`                   | Yes            | The email address of the user of this payment                     |
| `results[i].gateway_transaction_id`  | Yes            | The gateway transaction reference associated to this payment      |
| `results[i].status`                  | Yes            | The current external status of the payment                        |
| `results[i].created_date`            | Yes            | The created date in ISO_8601 format (```yyyy-MM-ddTHH:mm:ssZ```)  |
| `results[i].refund_summary.status`   | Yes            | The refund status of this payment                                 |
| `results[i].refund_summary.amount_available`| Yes     | The amount available for refunds for this payment                 |
| `results[i].refund_summary.amount_submitted`| Yes     | The total refund amount submitted for this payment                |
| `results[i].settlement_summary.captured_date`| No | Date of the capture according to the payment gateway                  |
| `results[i].settlement_summary.capture_submit_time`| No | Date and time of submission of the capture request              |
| `results[i].card_details.card_brand` | No             | The card brand used for this payment                              |
| `results[i].card_details.cardholder_name` | No        | The card card holder name of this payment                         |
| `results[i].card_details.expiry_date` | No            | The expiry date of this card                                      |
| `results[i].card_details.last_digits_card_number` | No| The last 4 digits of this card                                    |
| `results[i].card_details.first_digits_card_number` | No| The first 6 digits of this card                                    |
| `results[i].card_details.billing_address.line1` | No  | The line 1 of the billing address                                 |
| `results[i].card_details.billing_address.line2` | No  | The line 2 of the billing address                                 |
| `results[i].card_details.billing_address.postcode` | No| The postcode of the billing address                              |
| `results[i].card_details.billing_address.city` | No   | The city of the billing address                                   |
| `results[i].card_details.billing_address.country` | No| The country of the billing address                                |
| `results[i]._links.self`             | Yes            | Link to the payment                                               |
| `results[i]._links.events`           | Yes            | Link to payment events                                            |
| `results[i]._links.refunds`          | Yes            | Link to payment refunds                                           |
| `results[i]._links.cancel`           | No             | Link to cancel the payment (link only available when a payment can be cancelled (i.e. payment has one of the statuses - CREATED, IN PROGRESS |
| `_links.self.href`                | Yes            | Href link of the current page                                     |
| `_links.next_page.href`           | No             | Href link of the next page (based on the display_size requested)  |
| `_links.prev_page.href`           | No             | Href link of the previous page (based on the display_size requested) |
| `_links.first_page.href`          | Yes            | Href link of the first page (based on the display_size requested) |
| `_links.last_page.href`           | Yes            | Href link of the last page (based on the display_size requested)  |

### Response example for Direct Debit Payment
```
{
    "total": 3,
    "count": 1,
    "page": 2,
    "results": [
        {
            "amount": 200,
            "state": {
                "status": "pending",
                "finished": false
            },
            "agreement_id": "ncds8sfj22cvgrp90dk2nd",
            "description": "A test payment 2",
            "reference": "MBK71",
            "email": "citizen@example.com",
            "name": "Joe Bog",
            "transaction_id": "t9037r9pfla4q0cao1mq1ad3a7",
            "created_date": "2018-06-27T09:57:02.127Z",
            "links": {
                "self": {
                    "href": "https://publicapi.example.com/v1/payments/t9037r9pfla4q0cao1mq1ad3a7",
                    "method": "GET"
                }
            }
        }
    ],
    "_links": {
        "next_page": {
            "href": "https://publicapi.example.com/v1/payments?&page_number=3&display_size=100"
        },
        "self": {
            "href": "https://publicapi.example.com/v1/payments?&page_number=2&display_size=100"
        },
        "prev_page": {
            "href": "https://publicapi.example.com/v1/payments?&page_number=1&display_size=100"
        },
        "last_page": {
            "href": "https://publicapi.example.com/v1/payments?&page_number=3&display_size=100"
        },
        "first_page": {
            "href": "https://publicapi.example.com/v1/payments?&page_number=1&display_size=100"
        }
    }
}
```
#### Response field description for Direct Debit Payment
```
| Field                                 | Always present | Description                                                       |
| ------------------------------------- |:--------------:| ----------------------------------------------------------------- |
| `total`                               | Yes            | Total number of payments found                                    |
| `count`                               | Yes            | Number of payments displayed on this page                         |
| `page`                                | Yes            | Page number of the current recordset                              |
| `results`                             | Yes            | List of payments                                                  |
| `results[i].amount`                   | Yes            | The amount of this payment in pence                               |
| `results[i].state`                    | Yes            | The current external status of the payment                        |
| `results[i].description`              | Yes            | The payment description                                           |
| `results[i].reference`                | Yes            | There reference issued by the government service for this payment |
| `results[i].email`                    | Yes            | The email address of the user of this payment                     |
| `results[i].name`                     | Yes            | The name of the user of this payment                              |
| `results[i].payment_id`               | Yes            | The payment ID associated with this payment                     |
| `results[i].created_date`             | Yes            | The created date in ISO_8601 format (```yyyy-MM-ddTHH:mm:ssZ```)  |
| `results[i].agreement_id`             | Yes            | The external id of the agreement this payment was made against    |
| `results[i]._links.self`              | Yes            | Link to the payment                                               |
| `_links.self.href`                    | Yes            | Href link of the current page                                     |
| `_links.next_page.href`               | No             | Href link of the next page (based on the display_size requested)  |
| `_links.prev_page.href`               | No             | Href link of the previous page (based on the display_size requested) |
| `_links.first_page.href`              | Yes            | Href link of the first page (based on the display_size requested) |
| `_links.last_page.href`               | Yes            | Href link of the last page (based on the display_size requested)  |
```
### Search payments response errors

#### Validation errors
The search parameters are invalid

```
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/json
Content-Length: 44

{
    "code" : "P0401"
    "description" : "Invalid parameters: status, reference, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"
}
```

#### Page not found
Requested Page not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 44

{
    "code": "P0402"
    "description": "Page not found"
}
```

#### Unrecognised response from Connector
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 44

{
    "code" : "P0498"
    "description" : "Downstream system error"
}
```

#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P04XX                                        |
| `description`      | The error description                                                     |

#### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0401`            | Request parameters have Validation errors        |
| `P0402`            | Requested page not found                         |
| `P0498`            | Connector response was unrecognised to PublicAPI |

## POST /v1/agreements

This endpoint creates a new agreement.

### Request example

```
POST /v1/agreements
Authorization: Bearer BEARER_TOKEN
Content-Type: application/json
```
```json
{
    "return_url": "https://service.example.com/some-reference-to-this-agreement",
    "reference" : "some-reference-to-this-agreement"
}
```

#### Request description

BEARER_TOKEN: A valid bearer token for the account to associate the agreement with.

| Field                    | required | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `return_url`             | Yes      | The URL where the user should be redirected to when the agreement workflow is finished (**must be HTTPS only**). |
| `reference`              | No       | An optional reference that will be created by the service for easier identification of the future payments in their system. If not specified will be null |

### Agreement created response

```
HTTP/1.1 201 Created
Location: https://publicapi.example.com/v1/agreements/ab2341da231434
Content-Type: application/json
```
```json
{
    "_links": {
        "self" :{
            "href": "https://publicapi.example.com/v1/agreements/ab2341da231434",
            "method": "GET" 
        },
        "next_url" : {
            "href": "https://frontend.example.com/secure/ab2341da231434",
            "method": "GET" 
        },
        "next_url_post" : {
            "params" : {
                "chargeTokenId" : "82347"
            },
            "type" : "application/x-www-form-urlencoded",
            "href": "https://frontend.example.com/secure/ab2341da231434",
            "method": "POST" 
        }
    },
    "agreement_id": "ab2341da231434",
    "reference": "some-reference-to-this-agreement",
    "provider_id": "ab2341da231434",
    "return_url": "https://service.example.com/some-reference-to-this-agreement",
    "created_date": "2018-01-15T16:30:56Z",
    "state": "CREATED"
}
```

#### Response fields description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `agreement_id`         | The ID of the created agreement
| `reference`            | The reference passed into the request for this agreement
| `provider_id`          | A unique reference generated by the Direct Debit bureau
| `return_url`           | The URL where the user should be redirected to when the agreement workflow is finished.
| `created_date`         | The agreement creation date
| `state`                | Current state of the agreement
| `_links.self`          | Link to the agreement
| `_links.next_url`      | Where to navigate the user next as a GET
| `_links.next_url_post` | Where to navigate the user next as a POST

## GET /v1/refunds

This endpoint searches for refunds for the given account id, with filters and pagination

### Request example

```
GET /v1/refunds

```

#### Query Parameters description

| Field           | required | Description                               |
| --------------- |:--------:| ----------------------------------------- |
| `from_date`     |    -     | The initial date for search refunds |
| `to_date`       |    -     | The end date for search refunds |
| `page`          |    -     | To get the results from the specified page number, should be a non zero +ve number (optional, defaults to 1)|
| `display_size`  |    -     | Number of records to be returned per page, should be a non zero +ve number (optional, defaults to 500)|

### Response example for Card Payment

```
{
  "total": 5,
  "count": 2,
  "page": 2,
  "results": [
        {
            "refund_id": "quj60d2va9g106s3c6375flnt3",
            "created_date": "2018-10-25T15:56:26.297Z",
            "payment_id": "upd77pildong98a3cqqa6d8cso",
            "amount": 10000,
            "status": "submitted",
            "_links": {
                "self": {
                    "href": "https://publicapi.example.com/v1/payments/upd77pildong98a3cqqa6d8cso/refunds/quj60d2va9g106s3c6375flnt3",
                    "method": "GET"
                },
                "payment": {
                    "href": "https://publicapi.example.com/v1/payments/upd77pildong98a3cqqa6d8cso",
                    "method": "GET"
                }
            }
        },
        {
            "refund_id": "4bgf433166125hbb67a3dpqcid",
            "created_date": "2018-10-02T09:49:06.640Z",
            "payment_id": "5hd66phb4r3tud6q2i96g7h4mb",
            "amount": 1100,
            "status": "success",
            "_links": {
                "self": {
                    "href": "https://publicapi.example.com/v1/refunds/4bgf433166125hbb67a3dpqcid",
                    "method": "GET"
                },
                "payment": {
                    "href": "https://publicapi.example.com/v1/payments/5hd66phb4r3tud6q2i96g7h4mb",
                    "method": "GET"
                }
            }
        }
  ],
  "_links": {
    "next_page": {
      "href": "https://publicapi.example.com/v1/refunds?page=3&display_size=2"
    },
    "self": {
      "href": "https://publicapi.example.com/v1/refunds?page=2&display_size=2"
    },
    "prev_page": {
      "href": "https://publicapi.example.com/v1/refunds?page=1&display_size=2"
    },
    "last_page": {
      "href": "https://publicapi.example.com/v1/refunds?page=3&display_size=2"
    },
    "first_page": {
      "href": "https://publicapi.example.com/v1/refunds?page=1&display_size=2"
    }
  }
}
```

#### Response field description for Card Payment

| Field                             | Always present | Description                                                       |
| ------------------------          |:--------------:| ----------------------------------------------------------------- |
| `total`                           | Yes            | Total number of refunds found                                    |
| `count`                           | Yes            | Number of refunds displayed on this page                         |
| `page`                            | Yes            | Page number of the current recordset                              |
| `results`                         | Yes            | List of refunds                                                  |
| `results[i].payment_id`              | Yes            | The unique identifier for the original payment                            |
| `results[i].refund_id`                  | Yes            | The unique identifier for the refund                              |
| `results[i].created_date`            | Yes            | The created date in ISO_8601 format (```yyyy-MM-ddTHH:mm:ssZ```)  |
| `results[i].status`   | Yes            | The status of this refund                             |
| `results[i].amount`| Yes     | The total amount for this refund                |
| `results[i].fee`| No     |  processing fee taken by the GOV.UK Pay platform, in pence. Only available depending on payment service provider               |
| `results[i].net_amount`| No     | amount including all surcharges and less all fees, in pence. Only available depending on payment service provider         |
| `results[i]._links.self`             | Yes            | Link to the refund                                               |
| `results[i]._links.payment`           | Yes            | Link to the original payment                                          |
| `_links.self.href`                | Yes            | Href link of the current page                                     |
| `_links.next_page.href`           | No             | Href link of the next page (based on the display_size requested)  |
| `_links.prev_page.href`           | No             | Href link of the previous page (based on the display_size requested) |
| `_links.first_page.href`          | Yes            | Href link of the first page (based on the display_size requested) |
| `_links.last_page.href`           | Yes            | Href link of the last page (based on the display_size re

### Search refunds response errors

#### Validation errors
The search parameters are invalid

```
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/json
Content-Length: 44

{
    "code" : "P1101"
    "description" : "Invalid parameters: from_date, to_date, page, display_size. See Public API documentation for the correct data formats"
}
```

#### Page not found
Requested Page not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 44

{
    "code": "P1100"
    "description": "Page not found"
}
```

#### Unrecognised response from Connector
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 44

{
    "code" : "P1898"
    "description" : "Downstream system error"
}
```

#### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P1XXX                                        |
| `description`      | The error description                                                     |

#### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P1101`            | Request parameters have Validation errors        |
| `P1100`            | Requested page not found                         |
| `P1102`            | Refunds not supported for direct debit accounts  |
| `P1898`            | Connector response was unrecognised to PublicAPI |