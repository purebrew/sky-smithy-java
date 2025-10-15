/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package io.smithy.java.server.example;

import io.smithy.java.server.example.service.CoffeeShop;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import software.amazon.smithy.java.server.Server;

public class CafeService implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(CafeService.class.getName());

    public static void main(String... args) throws InterruptedException, ExecutionException {
        new CafeService().run();
    }

    @Override
    public void run() {
        Server server = Server.builder()
                .endpoints(URI.create("http://0.0.0.0:80"))
                .addService(
                        CoffeeShop.builder()
                                .addCreateOrderOperation(new CreateOrder())
                                .addGetMenuOperation(new GetMenu())
                                .addGetOrderOperation(new GetOrder())
                                .addListOrdersOperation(new ListOrder())
                                .build()
                )
                .build();
        LOGGER.info("Starting server...");
        server.start();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.info("Stopping server...");
            try {
                server.shutdown().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
