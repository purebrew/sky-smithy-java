package tech.purebrew.apigw;

import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.StringNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;

import software.amazon.smithy.model.traits.Trait;

public class JwtClaimTrait extends AbstractTrait {
    public static final ShapeId ID = ShapeId.from("tech.purebrew.apigw#jwtClaim");

    private final String claimName;

    public JwtClaimTrait(String claimName, FromSourceLocation sourceLocation) {
        super(ID, sourceLocation);
        this.claimName = claimName;
    }

    public JwtClaimTrait(String claimName) {
        this(claimName, SourceLocation.NONE);
    }

    public String getClaimName() {
        return this.claimName;
    }

    @Override
    protected Node createNode() {
        return new StringNode(this.claimName, this.getSourceLocation());
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(JwtClaimTrait.ID);
        }

        @Override
        public Trait createTrait(ShapeId shapeId, Node node) {
            return new JwtClaimTrait(node.expectStringNode().getValue(), node.getSourceLocation());
        }
    }
}
