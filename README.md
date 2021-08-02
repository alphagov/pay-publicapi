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

## Rate limiting

The application will rate-limit incoming API requests, recording the current
rate limit state in Redis (see `REDIS_URL` above). The rate-limiting behaviour
can be tuned via the following environment variables which all have default
values:

| Variable                           | Default      |  Description                               |
| ---------------------------------- | ------------ | ------------------------------------------ |
| `RATE_LIMITER_VALUE`               | Default 75   | Number of non-`POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds |
| `RATE_LIMITER_VALUE_POST`          | Default 15   | Number of `POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds |
| `RATE_LIMITER_ELEVATED_ACCOUNTS`   | N/A          | Comma-separated list of accounts to which `..._ELEVATED_...` limits apply (example: `1,2,3`) |
| `RATE_LIMITER_ELEVATED_VALUE_GET`  | Default 100  | Number of non-`POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds (for `RATE_LIMITER_ELEVATED_ACCOUNTS`) |
| `RATE_LIMITER_ELEVATED_VALUE_POST` | Default 40   | Number of `POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds (for `RATE_LIMITER_ELEVATED_ACCOUNTS`) |
| `RATE_LIMITER_VALUE_PER_NODE`      | Default 25   | Number of non-`POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds for a given client |
| `RATE_LIMITER_VALUE_PER_NODE_POST` | Default 5    | Number of `POST` requests allowed per `RATE_LIMITER_PER_MILLIS` milliseconds for a given client |
| `RATE_LIMITER_PER_MILLIS`          | Default 1000 | Rate limiter time window |

## API specification

Read our  [developer documentation](https://docs.payments.service.gov.uk/#gov-uk-pay-documentation) for guidance on using the API.

For more detailed information you can use our [OpenAPI specifiation](https://github.com/alphagov/pay-publicapi/blob/master/openapi/publicapi_spec.json)

## Dependencies

- https://www.mock-server.com/ is used for mocking dependent services

## Licence

[MIT License](LICENSE)

## Vulnerability Disclosure

GOV.UK Pay aims to stay secure for everyone. If you are a security researcher and have discovered a security vulnerability in this code, we appreciate your help in disclosing it to us in a responsible manner. Please refer to our [vulnerability disclosure policy](https://www.gov.uk/help/report-vulnerability) and our [security.txt](https://vdp.cabinetoffice.gov.uk/.well-known/security.txt) file for details.
