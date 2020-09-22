# ADR 001 - Use lettuce-core library instead of dropwizard redis

## Context

There was an [intention](https://payments-platform.atlassian.net/browse/PP-6343) to use the 
[dropwizard-redis](https://github.com/dropwizard/dropwizard-redis) library. The advantages of using this library are:

* Configuration
* Client lifecycle management
* Client health checks
* Dropwizard Metrics integration

The dropwizard-redis library's io.dropwizard.redis.RedisClientFactory class 
[expects](https://github.com/dropwizard/dropwizard-redis/blob/master/src/main/java/io/dropwizard/redis/RedisClientFactory.java#L54) 
tries to make a connection with Redis on application startup. If a connection cannot be made an exception will be thrown 
which causes the application to fail to start up. This has some unintended consequences:

* Mandating a connection on startup causes many integration tests to fail. Making a redis connection available for every relevant test is a fairly big change.
* Mandating a connection might affect running Publicapi in dev/local environments.

## Decision

We will use the [lettuce-core](https://github.com/lettuce-io/lettuce-core) instead. Reasons for this are:

* The dropwizard-library uses the lettuce-core libarary internally anyway
* Publicapi is currently designed to be able to work without Redis
* We used to use the old [dropwizard-client](https://github.com/benjamin-bader/droptools/tree/master/dropwizard-redis)
and were removing its healthcheck from the application anyway
* It is not hard to manually create a managed (lifecycle) service for lettuce-core
* lettuce-core seems to be more up to date; the old dropwizard-client hasn't been updated in a while

## Status

Accepted 