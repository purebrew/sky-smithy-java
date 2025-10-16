package tech.purebrew.apigw;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.utils.SmithyBuilder;
import software.amazon.smithy.utils.ToSmithyBuilder;

public class PublicServiceTrait extends AbstractTrait implements ToSmithyBuilder<PublicServiceTrait> {
    public static final ShapeId ID = ShapeId.from("tech.purebrew.apigw#publicService");

    private final String host;
    private final String gatewayBasePath;

    public PublicServiceTrait(Builder builder) {
        super(ID, builder.getSourceLocation());
        this.host = SmithyBuilder.requiredState("host", builder.host);
        this.gatewayBasePath = SmithyBuilder.requiredState("gatewayBasePath", builder.gatewayBasePath);
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId shapeId, Node node) {
            ObjectNode objectNode = node.expectObjectNode();
            return builder().sourceLocation(node)
                    .host(objectNode.expectStringMember("host").getValue())
                    .gatewayBasePath(objectNode.expectStringMember("gatewayBasePath").getValue())
                    .build();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHost() {
        return host;
    }

    public String getGatewayBasePath() {
        return gatewayBasePath;
    }

    @Override
    public Builder toBuilder() {
        return new Builder().host(host).gatewayBasePath(gatewayBasePath);
    }

    @Override
    protected Node createNode() {
        return Node.objectNodeBuilder()
                .sourceLocation(getSourceLocation())
                .withMember("host", Node.from(host))
                .withMember("gatewayBasePath", Node.from(gatewayBasePath))
                .build();
    }

    public static final class Builder extends AbstractTraitBuilder<PublicServiceTrait, Builder> {
        private String host;
        private String gatewayBasePath;

        @Override
        public PublicServiceTrait build() {
            return new PublicServiceTrait(this);
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder gatewayBasePath(String gatewayBasePath) {
            this.gatewayBasePath = gatewayBasePath;
            return this;
        }
    }
}
