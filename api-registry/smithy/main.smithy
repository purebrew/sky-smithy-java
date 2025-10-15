$version: "2"

namespace com.example

use aws.protocols#restJson1
use tech.purebrew.apigw#publicService

@title("Coffee Shop Service")
@restJson1
@publicService(host: "coffee-service", gatewayBasePath: "/v1/coffee")
service CoffeeShop {
    version: "2025-10-01"
    operations: [
        GetMenu
    ]
    resources: [
        Order
    ]
}

/// Retrieve the menu
@readonly
@http(method: "GET", uri: "/menu")
operation GetMenu {
    output := {
        items: CoffeeItems
    }
}
