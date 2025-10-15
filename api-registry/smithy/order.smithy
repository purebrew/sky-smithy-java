$version: "2.0"

namespace com.example

use tech.purebrew.apigw#jwtClaim

resource Order {
    identifiers: { id: Uuid }
    properties: { coffeeType: CoffeeType, status: OrderStatus, userId: UserId }
    read: GetOrder
    list: ListOrders
    create: CreateOrder
}

structure OrderSummary for Order {
    @required
    $id

    @required
    $coffeeType

    @required
    $status

    @required
    $userId
}

list OrderList {
    member: OrderSummary
}

/// Create an order
@idempotent
@http(method: "POST", uri: "/orders")
operation CreateOrder {
    input := for Order {
        @required
        $coffeeType

        @required
        @jwtClaim("sub")
        @httpHeader("X-User")
        $userId
    }

    output := for Order {
        @required
        $id

        @required
        $coffeeType

        @required
        $status

        @required
        $userId
    }
}

@error("client")
@httpError(409)
structure TooManyOrders {
    message: String
}

/// Retrieve an order
@readonly
@http(method: "GET", uri: "/orders/{id}")
operation GetOrder {
    input := for Order {
        @required
        @httpLabel
        $id
    }

    output := for Order {
        @required
        $id

        @required
        $coffeeType

        @required
        $status
    }

    errors: [
        OrderNotFound
    ]
}

@readonly
@http(method: "GET", uri: "/orders")
@paginated(inputToken: "nextToken", outputToken: "nextToken", items: "items", pageSize: "pageSize")
operation ListOrders {
    input := {
        @httpQuery("id")
        nextToken: String

        @httpQuery("pageSize")
        pageSize: Integer
    }

    output := for Order {
        items: OrderList

        @notProperty
        nextToken: String
    }
}

/// An error indicating an order could not be found
@error("client")
@httpError(404)
structure OrderNotFound {
    message: String
    orderId: Uuid
}

/// An identifier to describe a unique order
@length(min: 1, max: 128)
@pattern("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$")
string Uuid

/// An enum describing the status of an order
enum OrderStatus {
    IN_PROGRESS
    COMPLETED
}
