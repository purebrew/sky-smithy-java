package tech.purebrew.apigw;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.OperationIndex;

import software.amazon.smithy.model.traits.HttpHeaderTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.LinkedList;
import java.util.List;

public class JwtClaimValidator extends AbstractValidator {
    @Override
    public List<ValidationEvent> validate(Model model) {
        var events = new LinkedList<ValidationEvent>();
        var opIndex = OperationIndex.of(model);

        for (var member : model.getMemberShapesWithTrait(JwtClaimTrait.class)) {
            if (member.getTrait(HttpHeaderTrait.class).isEmpty()) {
                events.add(error(member, "JWT claims must be passed via HTTP headers. Please add the @httpHeader trait. " + member.getId()));
            }

            var structureId = member.getContainer();
            if (opIndex.getOutputBindings(structureId).size() + opIndex.getErrorBindings(structureId).size() > 0) {
                events.add(error(member, "JWT claim bindings can only be used in input shapes of an operations."));
            }

            opIndex.getOutputBindings(structureId).forEach(operation -> {
                var services = model
                    .getServiceShapes()
                    .stream()
                    .filter(service -> service.getAllOperations().stream().anyMatch(opId -> operation.getId() == opId));
            });
        }

        return events;
    }
}
