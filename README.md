# pay-publicapi

GOV.UK Pay Public API service in Java (Dropwizard)

## General configuration

Configuration of the application is performed via environment variables, some of which are mandatory.

| Variable                    | Required? | Default        | Description                                                                                                |
| --------------------------- | --------- | -------------- | ---------------------------------------------------------------------------------------------------------- |
| `ADMIN_PORT`                | No        | 8081           | The port number to listen for Dropwizard admin requests on.                                                |
| `ALLOW_HTTP_FOR_RETURN_URL` | No        | false          | Whether to allow service return URLs to be non-HTTPS                                                       |
| `CONNECTOR_DD_URL`          | Yes       | N/A            | The URL to the [direct-debit-connector](https://github.com/alphagov/pay-direct-debit-connector) service    |
| `CONNECTOR_URL`             | Yes       | N/A            | The URL to the [connector](https://github.com/alphagov/pay-connector) service                              |
| `DISABLE_INTERNAL_HTTPS`    | No        | false          | The port number to send graphite metrics to.                                                               |
| `METRICS_HOST`              | No        | localhost      | The hostname to send graphite metrics to.                                                                  |
| `METRICS_PORT`              | No        | 8092           | The port number to send graphite metrics to.                                                               |
| `PORT`                      | No        | 8080           | The port number to listen for requests on.                                                                 |
| `PUBLICAPI_BASE`            | Yes       | N/A            | The base URL clients can use to reach the API. e.g. http://api.example.org:1234/                           |
| `PUBLIC_AUTH_URL`           | Yes       | N/A            | The URL to the [publicauth](https://github.com/alphagov/pay-publicauth) service                            |
| `REDIS_URL`                 | No        | localhost:6379 | The location of the redis endpoint to store rate-limiter information in                                    |
| `TOKEN_API_HMAC_SECRET`     | Yes       | N/A            | Hmac secret to be used to validate that the given token is genuine (Api Key = Token + Hmac (Token, Secret) |

## Custom CA certificates

By default, the application will use the default Java truststore for validating
TLS connections. The docker startup script will add any PEM-format certificates
in `CERTS_PATH` to the default truststore prior to starting the application.

If `CERTS_PATH` is not specified, the default truststore will be used as-is.

| Variable     | Description                               |
| -------------| ----------------------------------------- |
| `CERTS_PATH` | A directory within the container containing CA certificates to add to the default Java truststore |

## Rate limiting

The application will rate-limit incoming API requests, recording the current
rate limit state in Redis (see `REDIS_URL` above). The rate-limiting behaviour
can be tuned via the following environment variables which all have default
values:

| Variable                           | Default      |  Description                               |
| ---------------------------------- | ------------ | ------------------------------------------ |
| `RATE_LIMITER_VALUE`               | Default 75   | Number of non-`POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds |
| `RATE_LIMITER_VALUE_POST`          | Default 15   | Number of `POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds |
| `RATE_LIMITER_VALUE_PER_NODE`      | Default 25   | Number of non-`POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds for a given client |
| `RATE_LIMITER_VALUE_PER_NODE_POST` | Default 5    | Number of `POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds for a given client |
| `RATE_LIMITER_PER_MILLIS`          | Default 1000 | Rate limiter time window |

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
|[`/v1/refunds`](docs/api_specification.md#get-v1refunds)  | GET    |  search/filter refunds           |
|[`/v1/directdebit/mandates`](docs/api_specification.md#post-v1directdebitmandates) | POST | create a mandate|
------------------------------------------------------------------------------------------------

## Dependencies

- https://www.mock-server.com/ is used for mocking dependent services

## Licence

[MIT License](LICENSE)

## Responsible Disclosure

GOV.UK Pay aims to stay secure for everyone. If you are a security researcher and have discovered a security vulnerability in this code, we appreciate your help in disclosing it to us in a responsible manner. We will give appropriate credit to those reporting confirmed issues. Please e-mail gds-team-pay-security@digital.cabinet-office.gov.uk with details of any issue you find, we aim to reply quickly.

