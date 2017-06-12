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

## API Specification

The [API Specification](docs/api_specification.md) provides more detail on the paths and operations including examples.

| Path                                                   | Method | Description                        |
| ------------------------------------------------------ | ------ | ---------------------------------- |
|[`/v1/payments`](docs/api_specification.md#post-v1payments)                      | POST   |  creates a payment                 |
|[`/v1/payments/{paymentId}`](docs/api_specification.md#get-v1paymentspaymentid)  | GET    |  returns a payment by ID           |
|[`/v1/payments/{paymentId}/cancel`](docs/api_specification.md#post-v1paymentspaymentidcancel)  | POST   |  cancels a payment |
|[`/v1/payments/{paymentId}/events`](docs/api_specification.md#get-v1paymentspaymentidevents)  | GET    |  returns all audit events for the payment referred by this ID  |
|[`/v1/payments`](docs/api_specification.md#get-v1payments)  | GET    |  search/filter payments           |
|[`/v1/payments/{paymentId}/refunds`](docs/api_specification.md#get-v1paymentspaymentidrefunds)| GET   |  returns a list of refunds for the payment|
|[`/v1/payments/{paymentId}/refunds`](docs/api_specification.md#post-v1paymentspaymentidrefunds)| POST |  creates a new refund for the payment     |
|[`/v1/payments/{paymentId}/refunds/{refundId}`](docs/api_specification.md#get-v1paymentspaymentidrefundsrefundid)| GET | returns a refund by ID   |

------------------------------------------------------------------------------------------------

## Licence

[MIT License](LICENSE)

## Responsible Disclosure

GOV.UK Pay aims to stay secure for everyone. If you are a security researcher and have discovered a security vulnerability in this code, we appreciate your help in disclosing it to us in a responsible manner. We will give appropriate credit to those reporting confirmed issues. Please e-mail gds-team-pay-security@digital.cabinet-office.gov.uk with details of any issue you find, we aim to reply quickly.

