# pay-publicapi
The Payments Public API in Java (Dropwizard)

## API

| Path                                                   | Method | Description                        |
| ------------------------------------------------------ | ------ | ---------------------------------- |
|[`/v1/payments`](#post-v1payments)                      | POST   |  creates a payment                 |
|[`/v1/payments/{paymentId}`](#get-v1paymentspaymentId)  | GET    |  returns a payment by ID           |



### POST /v1/payments

This endpoint creates a new payment.

#### Request example

```
POST /v1/payments
Content-Type: application/json

{
    "amount": 50000,
    "account_id": "32adf21bds3aac21"
    "return_url": "http://service.url/success"
}
```

##### Request body description

| Field                    | required | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `amount`                 | X | Amount to pay in pence                           |
| `account_id`             | X | ID of the account to use for the payment         |
| `return_url`             | X | The URL where the user should be redirected to when the payment workflow is finished.         |


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
        }
    ],
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "status": "CREATED"
}
```

##### Response field description

| Field                  | Description                               |
| ---------------------- | ----------------------------------------- |
| `payment_id`           | The ID of the created payment             |
| `amount`               | Amount to pay in pence                    |
| `status`               | Current status of the payment             |

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
        }
    ],
    "payment_id": "ab2341da231434",
    "amount": 50000,
    "status": "CREATED"
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
