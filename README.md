# pay-publicapi
GOV.UK Pay Public API service in Java (Dropwizard)

## Running in Development Mode

Steps are as follows:

1. Use a docker-compose environment to run everything (such as the database) that you don't want to develop on right now.
2. Stop `pay-publicapi` in the docker (`docker stop pay-publicapi`), to get ready to run from your checked out copy instead.
3. Because other services (inside or outside of docker) will try and talk to publicapi on dockerhost (not localhost), run the redirect script to send these requests to localhost.
4. Use `env.sh` to pick up the same environment variables from `pay-scripts`, so configuration is set correctly (this assumes `$WORKSPACE/pay-scripts` exists).

## Keystore setup for HTTPS outbound calls:

Following variables are needed in order to import the trusted certificates and public keys to a java keystore, which will be used for secure outbound HTTPS calls.
Importing certs/keys are handled in `docker-startup.sh`. This script assumes the infrastructure provids a trusted certificate file (CERT_FILE), a key (KEY_FILE) in a 
known directory (CERTS_DIR). 
Then the script creates a keystore (KEYSTORE_FILE) in a separate directory (KEYSTORE_DIR) and imports the certificate and key in to it.

| Variable                    | required |  Description                               |
| --------------------------- |:--------:| ------------------------------------------ |
| CERTS_DIR                   | X |  The directory where the import script can find a trusted certificate and any public key |
| CERT_FILE                   | X |  The name of the certificate file to import  |
| KEY_FILE                    | X |  The key file to import |
| KEYSTORE_DIR                | X |  The directory where the java keystore will be created |
| KEYSTORE_FILE               | X |  The name of the java keystore file |


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
| `amount`                 | X | Amount to pay in pence                           |
| `description`            | X | Payment description                              |
| `return_url`             | X | The URL where the user should be redirected to when the payment workflow is finished.         |
| `reference`              | X | There reference issued by the government service for this payment         |

The value of field `return_url` needs to contain a placeholder for the payment-id. This is the literal string `{paymentId}`,
which will be replaced with the payment-id of the created payment resource when the user finishes the payment workflow
and is redirected back to the calling service.

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
            "params" : {},
            "type" : "",
            "href": "http://frontend.co.uk/charge/1?chargeTokenId=82347",
            "method": "POST" 
        },
        "events" :{
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/events",
            "method": "GET" 
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://example.service.gov.uk/some-reference-to-this-payment",
    "reference": "some-reference-to-this-payment",
    "payment_provider": "Sandbox",
    "created_date": "2016-01-15T16:30:56Z"
}
```

##### Response field description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `payment_id`           | The ID of the created payment             |
| `amount`               | Amount to pay in pence                    |
| `description`          | Payment description                       |
| `status`               | Current status of the payment             |
| `return_url`           | The URL where the user should be redirected to when the payment workflow is finished.    |
| `reference`            | The reference issued by the government service for this payment                          |
| `payment_provider`     | The payment provider for this payment                                                    |
| `created_date`         | The payment creation date for this payment                                               |
| `_links.self`          | Link to the payment                                                                      |
| `_links.next_url`      | Where to navigate the user next as a GET                                                 |
| `_links.next_url_post` | Where to navigate the user next as a POST                                                |

#### Payment creation failed

```
HTTP/1.1 400 Bad Request
Content-Type: application/json
Content-Length: 34

{
    "message": "Unknown account: 32adf21bds3aac21"
}
```

##### Response field description

| Field              | Description                     |
| ------------------ | ------------------------------- |
| `message`          | The error message               |

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
        }
    },
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://example.service.gov.uk/some-reference-to-this-payment",
    "reference" : "some-reference-to-this-payment",
    "payment_provider": "Sandbox",
    "created_date": "2016-01-15T16:30:56Z"
}
```

##### Response field description

See: [Payment created](#payment-created-response)


#### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "message": "backend-error-message"
}
```

##### Response field description

See: [Payment creation failed](#payment-creation-failed)

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


#### Payment not found

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "message": "backend-error-message"
}
```

##### Response field description

See: [Payment creation failed](#payment-creation-failed)

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


#### Payment cancellation failed

Either because the payment state is not cancellable or the payment does not exist.

```
HTTP/1.1 400 Bad Request
Content-Type: application/json
Content-Length: 44

{
    "message": "Cancellation of charge failed."
}
```

### GET /v1/payments

This endpoint searches for transactions for the given account id.

#### Request example

```
GET /v1/payments

```

##### Query Parameters description

| Field                    | required | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `reference`              | - | There (partial or full) reference issued by the government service for this payment. |
| `status`                 | - | The transaction of this payment |
| `from_date`               | - | The initial date for search payments |
| `to_date`                 | - | The end date for search payments|

#### Response example

```
HTTP/1.1 200 OK
Content-Type: application/json
{
    "results": [{     
        "_links": {
                "self" :{
                    "href": "http://publicapi.co.uk/v1/payments/ab2341da231434",
                    "method": "GET" 
                },
                "events" :{
                    "href": "http://publicapi.co.uk/v1/payments/ab2341da231434/events",
                    "method": "GET" 
                }
        },
        "payment_id": "hu20sqlact5260q2nanm0q8u93",
        "payment_provider": "worldpay",
        "amount": 5000,
        "status": "CREATED",
        "description": "Your service description",
        "return_url": "http://your.service.domain/your-reference"
        "reference": "Ref-1234",
        "created_date": "2016-05-13T18:20:33Z"
     }]
}
```

##### Response field description

| Field                    | always present | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `results`                | X | List of payments       |
| `charge_id`              | X | The unique identifier for this charge       |
| `amount`                 | X | The amount of this charge in pence      |
| `description`            | X | The payment description       
| `reference`              | X | There reference issued by the government service for this payment       |
| `gateway_transaction_id` | X | The gateway transaction reference associated to this charge       |
| `status`                 | X | The current external status of the charge       |
| `created_date`           | X | The created date in ISO_8601 format (```yyyy-MM-ddTHH:mm:ssZ```)|
| `_links.self`            | X | Link to the payment                                                 |

-----------------------------------------------------------------------------------------------------------


## Responsible Disclosure

GOV.UK Pay aims to stay secure for everyone. If you are a security researcher and have discovered a security vulnerability in this code, we appreciate your help in disclosing it to us in a responsible manner. We will give appropriate credit to those reporting confirmed issues. Please e-mail gds-team-pay-security@digital.cabinet-office.gov.uk with details of any issue you find, we aim to reply quickly.
