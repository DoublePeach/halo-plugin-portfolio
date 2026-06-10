package run.halo.portfolio.reconciler;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.portfolio.extension.PortfolioOption;
import run.halo.portfolio.support.PortfolioExtensionSupport;

@Component
@RequiredArgsConstructor
public class PortfolioOptionReconciler implements Reconciler<Reconciler.Request> {

    static final String FINALIZER = "portfolio.plugin.halo.run/option-cleanup";

    private static final Set<String> ALLOWED_TYPES = Set.of(
        PortfolioOption.TYPE_TECH_STACK,
        PortfolioOption.TYPE_SOURCE,
        PortfolioOption.TYPE_DOMAIN
    );

    private final ExtensionClient client;
    private final PortfolioExtensionSupport support;

    @Override
    public Result reconcile(Request request) {
        var optionOpt = client.fetch(PortfolioOption.class, request.name());
        if (optionOpt.isEmpty()) {
            return Result.doNotRetry();
        }

        var option = optionOpt.get();
        var metadata = option.getMetadata();
        var finalizers = support.finalizersOf(metadata);

        if (!finalizers.contains(FINALIZER)) {
            support.addFinalizer(option, FINALIZER);
            return new Result(true, Duration.ofMillis(100));
        }

        if (metadata.getDeletionTimestamp() != null) {
            support.removeFinalizer(option, FINALIZER);
            return Result.doNotRetry();
        }

        syncValidation(option);
        return Result.doNotRetry();
    }

    private void syncValidation(PortfolioOption option) {
        var spec = option.getSpec();
        var invalidType = !ALLOWED_TYPES.contains(spec.getType());
        var invalidPortfolio = !support.portfolioExists(spec.getPortfolioName());
        var valueConflicts = support.findOptionValueConflicts(
            spec.getPortfolioName(),
            spec.getType(),
            spec.getValue(),
            option.getMetadata().getName()
        );
        var duplicateValue = !valueConflicts.isEmpty();

        var status = option.getStatus();
        if (status == null) {
            status = new PortfolioOption.OptionStatus();
            option.setStatus(status);
        }

        var changed = !Objects.equals(status.getInvalidType(), invalidType)
            || !Objects.equals(status.getInvalidPortfolio(), invalidPortfolio)
            || !Objects.equals(status.getDuplicateValue(), duplicateValue);

        if (!changed) {
            return;
        }

        status.setInvalidType(invalidType);
        status.setInvalidPortfolio(invalidPortfolio);
        status.setDuplicateValue(duplicateValue);
        option.setStatus(status);
        client.update(option);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new PortfolioOption())
            .build();
    }
}
