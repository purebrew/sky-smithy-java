#!/bin/bash

set -e

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "🌍 BUILDING THE PROTOCOL EXTENSIONS 🌍"
cd "$SCRIPT_DIR/protocol"
./gradlew publishToMavenLocal

echo "🗄️ BUILDING THE API REGISTRY 🗄️"
cd "$SCRIPT_DIR/api-registry"
./scala build-all.sc

echo "☕ BUILDING THE COFFEE SERVICE ☕ "
cd "$SCRIPT_DIR/coffee-service"
./gradlew distZip

cd "$SCRIPT_DIR"
mkdir -p swagger-ui/resources
cp api-registry/build/endpoints.tmpl apigw-krakend/config/templates/
cp api-registry/build/smithy/source/openapi/CoffeeShop.openapi.json swagger-ui/resources/internal.json
cp api-registry/build/smithy/publicServices/openapi/CoffeeShop.openapi.json swagger-ui/resources/public.json

echo "🐋 BUILDING DOCKER CONTAINERS 🐋"
docker-compose build
