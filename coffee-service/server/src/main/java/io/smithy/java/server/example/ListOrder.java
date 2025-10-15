package io.smithy.java.server.example;

import io.smithy.java.server.example.model.ListOrdersInput;
import io.smithy.java.server.example.model.ListOrdersOutput;
import io.smithy.java.server.example.service.ListOrdersOperation;
import software.amazon.smithy.java.server.RequestContext;

public class ListOrder implements ListOrdersOperation {
    @Override
    public ListOrdersOutput listOrders(ListOrdersInput input, RequestContext context) {
        return ListOrdersOutput.builder().items(
                OrderTracker.getOrders().stream().map(Order::summary).toList()
        ).build();
    }
}
