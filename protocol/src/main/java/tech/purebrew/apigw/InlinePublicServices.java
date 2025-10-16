package tech.purebrew.apigw;

import software.amazon.smithy.build.ProjectionTransformer;
import software.amazon.smithy.build.TransformContext;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.TopDownIndex;
import software.amazon.smithy.model.pattern.UriPattern;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.HttpBearerAuthTrait;
import software.amazon.smithy.model.traits.HttpTrait;

import java.util.Optional;
import java.util.stream.Collectors;

public class InlinePublicServices implements ProjectionTransformer {
    @Override
    public String getName() {
        return "inlinePublicServices";
    }

    @Override
    public Model transform(TransformContext context) {
        var model = context.getModel();
        var transformer = context.getTransformer();
        var topDownIndex = TopDownIndex.of(model);

        var publicServices = model.getServiceShapes().stream().filter(
                service -> service.getTrait(PublicServiceTrait.class).isPresent()
        ).toList();

        var publicOperationMap = publicServices.stream().filter(
                service -> service.getTrait(PublicServiceTrait.class).isPresent()
        ).flatMap(
                publicService -> topDownIndex
                        .getContainedOperations(publicService.getId())
                        .stream()
                        .map(op -> new PublicOperation(op, publicService.expectTrait(PublicServiceTrait.class)))
        ).collect(Collectors.toMap(
                pubOp -> pubOp.operation.getId(),
                pubOp -> pubOp.publicService
        ));

        var withBearerAuth =
                transformer.replaceShapes(
                        model,
                        publicServices
                                .stream()
                                .map(service ->
                                        Shape.shapeToBuilder(service)
                                                .addTrait(new HttpBearerAuthTrait())
                                                .removeTrait(PublicServiceTrait.ID)
                                                .build()
                                ).toList()
                );

//        var publicServiceIds = publicServices.stream().map(Shape::getId).collect(Collectors.toSet());
//        var withBearerAuth = transformer.mapShapes(model, shape ->
//           shape.getTrait(PublicServiceTrait.class).map(
//                    publicServiceTrait -> Shape.shapeToBuilder(shape).addTrait(new HttpBearerAuthTrait()).build()
//           ).orElse(shape)
//        );

//        var publicServiceTraitsRemoved = transformer.removeTraitsIf(
//                withBearerAuth,
//                (shape, trait) -> publicServiceIds.contains(shape.getId()) && trait instanceof PublicServiceTrait
//        );

        return transformer.mapTraits(withBearerAuth, (shape, trait) -> {
            if (trait instanceof HttpTrait httpTrait) {
                return Optional.ofNullable(publicOperationMap.get(shape.getId())).map(
                        publicServiceTrait ->
                                httpTrait
                                        .toBuilder()
                                        .uri(UriPattern.parse(
                                                publicServiceTrait.getGatewayBasePath() + httpTrait.getUri().toString()
                                        )).build()
                ).orElse(httpTrait);
            } else {
                return trait;
            }
        });
    }

    private record PublicOperation(Shape operation, PublicServiceTrait publicService) {}
}
