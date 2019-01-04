#!/usr/bin/env bash

set -eu

[ -z "${http_proxy:-}" ] || JAVA_OPTS="${JAVA_OPTS:-} -Dhttp.proxyHost=${http_proxy}"
[ -z "${https_proxy:-}" ] || JAVA_OPTS="${JAVA_OPTS:-} -Dhttps.proxyHost=${https_proxy}"
[ -z "${java_http_non_proxy_hosts:-}" ] || JAVA_OPTS="${JAVA_OPTS:-} -Dhttp.nonProxyHosts=${java_http_non_proxy_hosts}"

java $JAVA_OPTS -jar *-allinone.jar server *.yaml
