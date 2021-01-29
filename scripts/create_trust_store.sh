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

print_green "fetching service ca"
credhub get -n /services/tls_ca --key=certificate > /tmp/services_tls_ca.crt

print_green "fetching go router ca"
# this may also be retrieved via credhub /opsmgr/cf-c6cac3292bcb6f4af1d0/networking_poe_ssl_certs/0/certificate
host=$(jq -r .urls.gfsh < "${service_key}" | cut -d'/' -f3)
echo "quit" | openssl s_client -showcerts -connect ${host}:443 |  sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/go_router_ca.crt

keystore="$HOME/workspace/PCC-Sample-App-PizzaStore/src/main/resources/services_gateway_truststore.jks"

print_green "using key store ${keystore}"
rm "${keystore}"

print_green "importing service ca to key_tool"
keytool -importcert \
 -alias service_ca \
 -file /tmp/services_tls_ca.crt \
 -keystore "${keystore}" \
 -storetype JKS \
 -storepass 123456 \
 -noprompt

print_green "importing go router ca to key_tool"
keytool -importcert \
 -alias go_router_ca \
 -file /tmp/go_router_ca.crt \
 -keystore "${keystore}" \
 -storetype JKS \
 -storepass 123456 \
 -noprompt

print_green "keystore contents"
keytool -list -storepass 123456 -v -keystore "${keystore}"