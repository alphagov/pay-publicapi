# pay-publicapi
GOV.UK Pay Public API service in Java (Dropwizard)

## Running in Development Mode

Steps are as follows:

1. Use a docker-compose environment to run everything (such as the database) that you don't want to develop on right now.
2. Stop `pay-publicapi` in the docker (`docker stop pay-publicapi`), to get ready to run from your checked out copy instead.
3. Because other services (inside or outside of docker) will try and talk to publicapi on dockerhost (not localhost), run the redirect script to send these requests to localhost.
4. Use `env.sh` to pick up the same environment variables from `pay-scripts`, so configuration is set correctly (this assumes `$WORKSPACE/pay-scripts` exists).

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

## API

| Path                                                   | Method | Description                        |
| ------------------------------------------------------ | ------ | ---------------------------------- |
|[`/v1/payments`](#post-v1payments)                      | POST   |  creates a payment                 |
|[`/v1/payments/{paymentId}`](#get-v1paymentspaymentid)  | GET    |  returns a payment by ID           |
|[`/v1/payments/{paymentId}/cancel`](#post-v1paymentspaymentidcancel)  | POST   |  cancels a payment |


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
    "links": [
        {
            "rel": "self",
            "method": "GET",
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434"
        },
        {
            "rel": "next_url",
            "method": "GET",
            "href": "http://frontend.co.uk/charge/1?chargeTokenId=82347"
        }
    ],
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "https://example.service.gov.uk/some-reference-to-this-payment",
    "reference": "some-reference-to-this-payment"
}
```

##### Response field description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `payment_id`           | The ID of the created payment             |
| `amount`               | Amount to pay in pence                    |
| `description`          | Payment description                       |
| `status`               | Current status of the payment             |
| `return_url`           | The URL where the user should be redirected to when the payment workflow is finished.         |
| `reference`            | There reference issued by the government service for this payment          |

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
    "links": [
        {
            "rel": "self",
            "method": "GET",
            "href": "http://publicapi.co.uk/v1/payments/ab2341da231434"
        },
        {
            "rel": "next_url",
            "method": "GET",
            "href": "http://frontend.co.uk/charge/1?chargeTokenId=82347"
        }
    ],
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "description": "Payment description",
    "status": "CREATED",
    "return_url": "{service/return/url/for/this/payment}",
    "reference" : "abcd-1234" 
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
