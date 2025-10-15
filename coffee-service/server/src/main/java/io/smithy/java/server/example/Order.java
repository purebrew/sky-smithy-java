/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package io.smithy.java.server.example;

import io.smithy.java.server.example.model.CoffeeType;
import io.smithy.java.server.example.model.OrderStatus;
import io.smithy.java.server.example.model.OrderSummary;

import java.util.UUID;

/**
 * A coffee drink order.
 *
 * @param id UUID of the order
 * @param type Type of drink for the order
 * @param status status of the order
 * @param userId the User who placed the order
 */
public record Order(UUID id, CoffeeType type, OrderStatus status, String userId) {
    public OrderSummary summary() {
        return OrderSummary.builder()
                .id(id.toString())
                .coffeeType(type)
                .status(status)
                .userId(userId)
                .build();
    }
}
