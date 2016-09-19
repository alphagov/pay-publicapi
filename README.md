# pay-publicapi
GOV.UK Pay Public API service in Java (Dropwizard)

## Keystore setup for HTTPS outbound calls:

Following variables are needed in order to import the trusted certificates and public keys to a java keystore, which will be used for secure outbound HTTPS calls.
Importing certs/keys are handled in `docker-startup.sh`. This script assumes the infrastructure provids a trusted certificate file (CERT_FILE), a key (KEY_FILE) in a 
known directory (CERTS_DIR). 
Then the script creates a keystore (KEYSTORE_FILE) in a separate directory (KEYSTORE_DIR) and imports the certificate and key in to it.

| Variable                    | required |  Description                               |
| --------------------------- |:--------:| ------------------------------------------ |
| CERTS_DIR                   | Yes      |  The directory where the import script can find a trusted certificate and any public key |
| CERT_FILE                   | Yes      |  The name of the certificate file to import  |
| KEY_FILE                    | Yes      |  The key file to import |
| KEYSTORE_DIR                | Yes      |  The directory where the java keystore will be created |
| KEYSTORE_FILE               | Yes      |  The name of the java keystore file |

## Rate limiter and Authorization filters setup

These ara the variables related to Public API filters.

| Variable                    | required         |  Description                               |
| --------------------------- | -----------------| ------------------------------------------ |
| RATE_LIMITER_VALUE          | No (Default 3)   | Number of requests allowed per time defined by RATE_LIMITER_PER_MILLIS |
| RATE_LIMITER_PER_MILLIS     | No (Default 1000)| Rate limiter time window |
| TOKEN_API_HMAC_SECRET       | Yes              | Hmac secret to be used to validate that the given token is genuine (Api Key = Token + Hmac (Token, Secret) |

For example:

```
$ ./redirect.sh start
$ ./env.sh mvn exec:java
...
(pay-publicapi log output)
...
(press CTRL+C to stop service)
...
$ ./redirect.sh stop
```

## API through gelato.io 

gelato.io is a hosted service that dynamically generates beautiful documentation and sandbox from a Swagger-compliant API.
It also provides customized documentation, markdown editor, automatic API explorer, code sample Generation, custom styling and allows to add a custom domain.

Useful links:
 - [API Portal](https://gds-payments.gelato.io)
 - [API Documentation](https://gds-payments.gelato.io/reference/docs)
 - [API Reference](https://gds-payments.gelato.io/reference/api/v1)
 - [API Explorer](https://gds-payments.gelato.io/api-explorer/)


## API

| Path                                                   | Method | Description                        |
| ------------------------------------------------------ | ------ | ---------------------------------- |
|[`/v1/payments`](#post-v1payments)                      | POST   |  creates a payment                 |
|[`/v1/payments/{paymentId}`](#get-v1paymentspaymentid)  | GET    |  returns a payment by ID           |
|[`/v1/payments/{paymentId}/cancel`](#post-v1paymentspaymentidcancel)  | POST   |  cancels a payment |
|[`/v1/payments/{paymentId}/events`](#get-v1paymentspaymentidevents)  | GET    |  returns all audit events for the payment referred by this ID  |
|[`/v1/payments`](#get-v1payments)  | GET    |  search/filter payments           |
|[`/v1/payments/{paymentId}/refunds`](#get-v1paymentspaymentidrefunds)| GET   |  returns a list of refunds for the payment|
|[`/v1/payments/{paymentId}/refunds`](#post-v1paymentspaymentidrefunds)| POST |  creates a new refund for the payment     |
|[`/v1/payments/{paymentId}/refunds/{refundId}`](#get-v1paymentspaymentidrefundsrefundid)| GET | returns a refund by ID   |

------------------------------------------------------------------------------------------------

### POST /v1/payments

This endpoint creates a new payment.

#### Request example

```
POST /v1/payments
Authorization: Bearer BEARER_TOKEN
Content-Type: application/json

{
    "amount": 50000,
    "description": "Payment description",
    "return_url": "https://example.service.gov.uk/some-reference-to-this-payment",
    "reference" : "some-reference-to-this-payment"
}
```

##### Request description

BEARER_TOKEN: A valid bearer token for the account to associate the payment with.

| Field                    | required | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `amount`                 | Yes      | Amount to pay in pence                           |
| `description`            | Yes      | Payment description                              |
| `return_url`             | Yes      | The URL where the user should be redirected to when the payment workflow is finished.         |
| `reference`              | Yes      | There reference issued by the government service for this payment         |

#### Payment created response

```
HTTP/1.1 201 Created
Location: http://publicapi.co.uk/v1/payments/ab2341da231434
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434",
            "method": "GET" 
        },
        "next_url" : {
            "href": "http://frontend.co.uk/charge/1?chargeTokenId=82347",
            "method": "GET" 
        },
        "next_url_post" : {
            "params" : {
                "chargeTokenId" : "82347"
            },
            "type" : "application/x-www-form-urlencoded",
            "href": "http://frontend.co.uk/charge/1?chargeTokenId=82347",
            "method": "POST" 
        },
        "events" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/events",
            "method": "GET" 
        },
        "refunds" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/refunds",
            "method": "GET" 
        },
        "cancel" : {
            "params" : {},
            "type" : "",
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/cancel",
            "method": "POST" 
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://example.service.gov.uk/some-reference-to-this-payment",
    "reference": "some-reference-to-this-payment",
    "payment_provider": "Sandbox",
    "card_brand": "Mastercard",
    "created_date": "2016-01-15T16:30:56Z",
    "refund_summary": {
        "status": "available"
        "amount_available": 14500
        "amount_submitted": 0
    }
}
```

##### Response fields description

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
| `created_date`         | The payment creation date for this payment                                               |
| `_links.self`          | Link to the payment                                                                      |
| `_links.next_url`      | Where to navigate the user next as a GET                                                 |
| `_links.next_url_post` | Where to navigate the user next as a POST                                                |
| `_links.events`        | Link to payment events                                                |
| `_links.refunds`       | Link to payment refunds                                                |
| `_links.cancel`        | Link to cancel the payment (link only available when a payment can be cancelled (i.e. payment has one of the statuses - CREATED, IN PROGRESS |

#### Payment creation response errors

##### Unrecognised response from Connector
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

##### Validation errors
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

##### Missing, null or empty mandatory field
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

##### Unable to parse request body
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

##### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `field`            | Field related to the error (Only for validation or missing fields errors) |
| `code`             | The error reference. Format: P01XX                                        |
| `description`      | The error description                                                     |

##### Response error codes

| Code               | Description                                                       |
| ------------------ | ------------------------------------------------------------------|
| `P0199`            | Auth token was correct but the account wasn't found in Connector  |
| `P0198`            | Connector response was unrecognised to PublicAPI                  |
| `P0100`            | Body sent by the client can't be processed (is not a valid JSON)  |
| `P0101`            | An mandatory attribute in the JSON body is missing, null or empty |
| `P0102`            | An attribute in the JSON body has a validation error              |

------------------------------------------------------------------------------------------------

### GET /v1/payments/{paymentId}

Returns a payment by ID.

#### Request example

```
GET /v1/payments/ab2341da231434
Authorization: Bearer BEARER_TOKEN
```

#### Payment response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434",
            "method": "GET" 
        },
        "next_url" : {
            "href": "http://frontend.co.uk/charge/ab2341da231434?chargeTokenId=82347",
            "method": "GET" 
        },
        "next_url_post" : {
            "params" : {},
            "type" : "",
            "href": "http://frontend.co.uk/charge/1?chargeTokenId=82347",
            "method": "POST" 
        },
        "events" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/events",
            "method": "GET" 
        },
        "refunds" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/refunds",
            "method": "GET" 
        },
        "cancel" : {
            "params" : {},
            "type" : "",
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/cancel",
            "method": "POST"
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://example.service.gov.uk/some-reference-to-this-payment",
    "reference" : "some-reference-to-this-payment",
    "email": "mail@email.com",
    "payment_provider": "Sandbox",
    "card_brand": "Visa",
    "created_date": "2016-01-15T16:30:56Z",
    "refund_summary": {
        "status": "available"
        "amount_available": 14500
        "amount_submitted": 0
    }
}
```

##### Response field description

See: [Payment created](#payment-created-response)

#### GET Payment response errors

##### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0200"
    "description": "Not found"
}
```

##### Unrecognised response from Connector

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
    "code" : "P0298"
    "description": "Downstream system error"
}
```

##### Response errors field description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P02XX                                        |
| `description`      | The error description                                                     |

##### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0200`            | Connector response was 404 Not Found             |
| `P0298`            | Connector response was unrecognised to PublicAPI |

------------------------------------------------------------------------------------------------

### GET /v1/payments/{paymentId}/events

Returns the list of events associated with a payment.

#### Request example

```
GET /v1/payments/ab2341da231434/events
Authorization: Bearer BEARER_TOKEN
```

#### Payment response

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
                    "href": "http://publicapi.co.uk/v1/payments/ab2341da231434"
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
                    "href": "http://publicapi.co.uk/v1/payments/ab2341da231434"
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
                    "href": "http://publicapi.co.uk/v1/payments/ab2341da231434"
                }
            }
        }
    ],
    "_links": {
        "self" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434",
            "method": "GET" 
        }           
    },
    "payment_id": "ab2341da231434"

```

##### Response field description

See: [Payment created](#payment-created-response)

#### GET Payment Events response errors

##### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0300"
    "description": "Not found"
}
```

##### Unrecognised response from Connector

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
    "code" : "P0398"
    "description": "Downstream system error"
}
```

##### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |

##### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0300`            | Connector response was 404 Not Found             |
| `P0398`            | Connector response was unrecognised to PublicAPI |

------------------------------------------------------------------------------------------------
### GET /v1/payments/{paymentId}/refunds

Returns a list of refunds associated with a payment.

#### Request example

```
GET /v1/payments/ab2341da231434/refunds
Authorization: Bearer BEARER_TOKEN
```

#### Payment refunds response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "payment_id": "ab2341da231434",
  "_links": {
    "self": {
      "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/refunds"
    },
    "payment": {
      "href": "http://publicapi.co.uk/v1/payments/ab2341da231434"
    }
  },
  "_embedded": {
    "refunds": [
      {
  	    "_links": {
          "self" :{
            "href": "http://publicapi.co.uk/v1/payments/abc123/refunds/xyz123",
          },
         "payment" :{
           "href": "http://publicapi.co.uk/v1/payments/abc123",
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
                "href": "http://publicapi.co.uk/v1/payments/abc123/refunds/xyz124"
            },
            "payment" :{
                "href": "http://publicapi.co.uk/v1/payments/abc123"              
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

##### Response field description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `payment_id`           | The ID of the created payment             |
| `_embedded.refunds.refund_id`               | The ID of this refund                    |
| `_embedded.refunds.amount`          | The amount of refund                       |
| `_embedded.refunds.status`               | Current status of the refund (submitted/success)             |
| `_embedded.refunds.created_date`           | Date when the refund was created    |

#### GET Payment Refunds response errors

##### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0800"
    "description": "Not found"
}
```
##### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |

------------------------------------------------------------------------------------------------
### GET /v1/payments/{paymentId}/refunds/{refundId}

Returns the refund by ID for the payment.

#### Request example

```
GET /v1/payments/ab2341da231434/refunds/xyz124
Authorization: Bearer BEARER_TOKEN
```

#### Payment refund response

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
            "href": "http://publicapi.co.uk/v1/payments/abc123/refunds/xyz124"
        }
        "payment": {
            "href": "http://publicapi.co.uk/v1/payments/abc123"
        }
    }
}
```

##### Response field description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `refund_id`            | The ID of the refund                      |
| `amount`               | Amount in pence for the refund            |
| `status`               | Current status of the refund (submitted/success)             |
| `created_date`         | Date when the refund was created          |
| `_links.self`          | Self link for this refund                 |
| `_links.payment`       | Link for the payment of this refund       |

#### GET Payment Refunds response errors

##### Refud ID not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0700"
    "description": "Not found"
}
```
##### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |


------------------------------------------------------------------------------------------------
### POST /v1/payments/{paymentId}/refunds

Creates a new refund associated with the payment.

#### Request example

```
POST /v1/payments/ab2341da231434/refunds
Authorization: Bearer BEARER_TOKEN
{
    "amount": 25000
}
```

##### Request description

BEARER_TOKEN: A valid bearer token for the account to associate the payment with.

| Field                    | required | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `amount`                 | Yes      | Amount to refund in pence                 |

#### Refund created response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "_links": {
        "self" :{
            "href": "http://publicapi.co.uk/v1/payments/abc123/refunds/xyz123",

        },

        "payment_url" :{
            "href": "http://publicapi.co.uk/v1/payments/abc123",

        },
    }
    "refund_id": "x2aysfsg3a3s45z123",
    "amount": 25000,
    "status": "submitted",
    "created_date": "2016-06-01 16:34:23",
}
```

##### Response fields description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `refund_id`            | The ID of the refund created             |
| `amount`               | Amount of refund in pence                |
| `status`               | Current status of the refund             |
| `created_date`         | The creation date for this refund        |
| `_links.self`          | Link to this refund                      |
| `_links.payment`      | Link to the payment this refund relates to|

#### POST Payment Refunds response errors

##### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "code" : "P0600"
    "description": "Not found"
}
```
##### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P03XX                                        |
| `description`      | The error description                                                     |
------------------------------------------------------------------------------------------------
### POST /v1/payments/{paymentId}/cancel

This endpoint cancels a new payment. A payment can only be cancelled if it's state is one of the following (case-insensitive):

| Cancellable payment states |
| -------------------------- |
| Created                    |
| In Progress                |


#### Request example

```
POST /v1/payments/ab2341da231434/cancel
Authorization: Bearer BEARER_TOKEN
```

#### Payment cancellation successful

```
HTTP/1.1 204 No Content
```

#### Payment cancellation response errors

##### Payment not found

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

##### Payment cancellation failed

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

##### Unrecognised response from Connector

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

##### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P05XX                                        |
| `description`      | The error description                                                     |


##### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0500`            | Connector response was 404 Not Found             |
| `P0501`            | Connector response was 400 Bad Request           |
| `P0598`            | Connector response was unrecognised to PublicAPI |

------------------------------------------------------------------------------------------------
### GET /v1/payments

This endpoint searches for transactions for the given account id, with filters and pagination

#### Request example

```
GET /v1/payments

```

##### Query Parameters description

| Field                     | required | Description                               |
| ------------------------  |:--------:| ----------------------------------------- |
| `reference`               | - | There (partial or full) reference issued by the government service for this payment. |
| `status`                  | - | The transaction of this payment |
| `from_date`               | - | The initial date for search payments |
| `to_date`                 | - | The end date for search payments|
| `card_brand`              | - | The card brand for search payments|
| `page`                    | - | To get the results from the specified page number, should be a non zero +ve number (optional, defaults to 1)|
| `display_size`            | - | Number of records to be returned per page, should be a non zero +ve number (optional, defaults to 500)|
| `email`                   | - | Email ID of the payment user to search for          |

#### Response example

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
      "return_url": "https://demoservice.pymnt.localdomain:443/return/rahul-ref",
      "reference": "rahul-ref",
      "email": "mail@email.com",
      "created_date": "2016-05-23T15:22:50.972Z",
      "refund_summary": {
         "status": "pending"
         "amount_available": 1
         "amount_submitted": 0
      }
      "_links": {
        "self": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/4hn0c8bbtfbnp5tmite2274h5c",
          "method": "GET"
        },
        "cancel": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/4hn0c8bbtfbnp5tmite2274h5c/cancel",
          "method": "POST"
        },
        "events": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/4hn0c8bbtfbnp5tmite2274h5c/events",
          "method": "GET"
        },
        "refunds": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/br0ih3laeacuf3845j4q77d11p/refunds"
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
      "return_url": "https://demoservice.pymnt.localdomain:443/return/rahul-ref",
      "reference": "rahul-ref",
      "email": "mail@email.com",
      "created_date": "2016-05-23T15:22:47.038Z",
      "refund_summary": {
         "status": "pending"
         "amount_available": 1
         "amount_submitted": 0
      }
      "_links": {
        "self": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/am6f5d1583563deb7ss5obju2",
          "method": "GET"
        },
        "cancel": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/am6f5d1583563deb7ss5obju2/cancel",
          "method": "POST"
        },
        "events": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/am6f5d1583563deb7ss5obju2/events",
          "method": "GET"
        }
        "refunds": {
          "href": "https://publicapi.pymnt.localdomain/v1/payments/br0ih3laeacuf3845j4q77d11p/refunds"
          "method": "GET"
        }
      }
    }
  ],
  "_links": {
    "next_page": {
      "href": "https://publicapi.pymnt.localdomain/v1/payments?page=3&display_size=2"
    },
    "self": {
      "href": "https://publicapi.pymnt.localdomain/v1/payments?page=2&display_size=2"
    },
    "prev_page": {
      "href": "https://publicapi.pymnt.localdomain/v1/payments?page=1&display_size=2"
    },
    "last_page": {
      "href": "https://publicapi.pymnt.localdomain/v1/payments?page=3&display_size=2"
    },
    "first_page": {
      "href": "https://publicapi.pymnt.localdomain/v1/payments?page=1&display_size=2"
    }
  }
}```

##### Response field description

| Field                             | Always present | Description                                                       |
| ------------------------          |:--------------:| ----------------------------------------------------------------- |
| `total`                           | Yes            | Total number of payments found                                    |
| `count`                           | Yes            | Number of payments displayed on this page                         |
| `page`                            | Yes            | Page number of the current recordset                              |
| `results`                         | Yes            | List of payments                                                  |
| `results.payment_id`              | Yes            | The unique identifier for this payment                            |
| `results.amount`                  | Yes            | The amount of this payment in pence                               |
| `results.card_brand`              | Yes            | The card brand used for this payment                              |
| `results.description`             | Yes            | The payment description                                           |
| `results.reference`               | Yes            | There reference issued by the government service for this payment |
| `results.email`                   | Yes            | The email address of the user of this payment                     |
| `results.gateway_transaction_id`  | Yes            | The gateway transaction reference associated to this payment      |
| `results.status`                  | Yes            | The current external status of the payment                        |
| `results.created_date`            | Yes            | The created date in ISO_8601 format (```yyyy-MM-ddTHH:mm:ssZ```)  |
| `results.refund_summary.status`   | Yes            | The refund status of this payment                                 |
| `results.refund_summary.amount_available`| Yes     | The amount available for refunds for this payment                 |
| `results.refund_summary.amount_submitted`| Yes     | The total refund amount submitted for this payment                |
| `results._links.self`             | Yes            | Link to the payment                                               |
| `results._links.events`           | Yes            | Link to payment events                                            |
| `results._links.refunds`          | Yes            | Link to payment refunds                                           |
| `results._links.cancel`           | No             | Link to cancel the payment (link only available when a payment can be cancelled (i.e. payment has one of the statuses - CREATED, IN PROGRESS |
| `_links.self.href`                | Yes            | Href link of the current page                                     |
| `_links.next_page.href`           | No             | Href link of the next page (based on the display_size requested)  |
| `_links.prev_page.href`           | No             | Href link of the previous page (based on the display_size requested) |
| `_links.first_page.href`          | Yes            | Href link of the first page (based on the display_size requested) |
| `_links.last_page.href`           | Yes            | Href link of the last page (based on the display_size requested)  |
```

#### Search payments response errors

##### Validation errors
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

##### Page not found
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

##### Unrecognised response from Connector
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 44

{
    "code" : "P0498"
    "description" : "Downstream system error"
}
```

##### Response error fields description

| Field              | Description                                                               |
| ------------------ | --------------------------------------------------------------------------|
| `code`             | The error reference. Format: P04XX                                        |
| `description`      | The error description                                                     |

##### Response error codes

| Code               | Description                                      |
| ------------------ | -------------------------------------------------|
| `P0401`            | Request parameters have Validation errors        |
| `P0402`            | Requested page not found                         |
| `P0498`            | Connector response was unrecognised to PublicAPI |

## Responsible Disclosure

GOV.UK Pay aims to stay secure for everyone. If you are a security researcher and have discovered a security vulnerability in this code, we appreciate your help in disclosing it to us in a responsible manner. We will give appropriate credit to those reporting confirmed issues. Please e-mail gds-team-pay-security@digital.cabinet-office.gov.uk with details of any issue you find, we aim to reply quickly.
