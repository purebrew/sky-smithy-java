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
cp api-registry/build/endpoints.tmpl apigw-krakend/config/templates/

