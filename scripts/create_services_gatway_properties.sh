#!/bin/bash

function print_green() {
    local green='\033[0;32m'
    local default='\033[0m'

    printf "${green}${1}${default}\n" 1>&2
}

function print_red() {
    local red='\033[0;31m'
    local default='\033[0m'

    printf "${red}${1}${default}\n" 1>&2
}

function print_usage() {
  print_red "Try one of these:
       ./create_services_gateway_properties.sh <service_key_location>"
}

if [[ $# -lt 1 ]]; then
    print_red "not enough arguments"
    print_usage
    exit 1
fi

service_key=$1

print_green "using service key at: ${service_key}"

locators=$(jq -jr .locators[] < "${service_key}")
service_gateway_hostname=$(jq -r .service_gateway.host < "${service_key}")
service_gateway_port=$(jq -r .service_gateway.port < "${service_key}")

security=$(jq -r '.users[] | select(.username|test("cluster_operator_.*"))' < "${service_key}")
security_username=$(echo "${security}" | jq -r .username)
security_password=$(echo "${security}" | jq -r .password)

managment_host=$(jq -r .urls.gfsh < "${service_key}" | cut -d'/' -f3)

cat > src/main/resources/application-app-foundation.properties <<HEREDOC
# Demonstrates the case where the app is running a Application Foundation
# and the service instance in running in a different Foundation (Services Foundation)

spring.data.gemfire.pool.locators=${locators}
service-gateway.hostname=${service_gateway_hostname}
service-gateway.port=${service_gateway_port}
spring.data.gemfire.pool.default.socket-factory-bean-name=mySocketFactory
spring.data.gemfire.security.username=${security_username}
spring.data.gemfire.security.password=${security_password}

#http.host and http.port are needed if you use @EnableClusterConfiguration
# http.host is the hostname part from the gfsh url in the service key, port is always 443
spring.data.gemfire.management.http.host=${managment_host}
spring.data.gemfire.management.http.port=443

#TLS
spring.data.gemfire.security.ssl.components=all
gemfire.ssl-truststore=/home/vcap/app/BOOT-INF/classes/services_gateway_truststore.jks
gemfire.ssl-truststore-password=123456
gemfire.ssl-keystore=
gemfire.ssl-keystore-password=

HEREDOC