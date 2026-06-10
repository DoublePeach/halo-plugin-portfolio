package run.halo.portfolio.reconciler;

import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.portfolio.extension.Portfolio;
import run.halo.portfolio.support.PortfolioExtensionSupport;

@Component
@RequiredArgsConstructor
public class PortfolioReconciler implements Reconciler<Reconciler.Request> {

    static final String FINALIZER = "portfolio.plugin.halo.run/portfolio-cleanup";

    private final ExtensionClient client;
    private final PortfolioExtensionSupport support;

    @Override
    public Result reconcile(Request request) {
        var portfolioOpt = client.fetch(Portfolio.class, request.name());
        if (portfolioOpt.isEmpty()) {
            return Result.doNotRetry();
        }

        var portfolio = portfolioOpt.get();
        var metadata = portfolio.getMetadata();
        var finalizers = support.finalizersOf(metadata);

        if (!finalizers.contains(FINALIZER)) {
            support.addFinalizer(portfolio, FINALIZER);
            return new Result(true, Duration.ofMillis(100));
        }

        if (metadata.getDeletionTimestamp() != null) {
            if (support.hasPortfolioChildren(metadata.getName())) {
                return Result.doNotRetry();
            }
            support.removeFinalizer(portfolio, FINALIZER);
            return Result.doNotRetry();
        }

        syncSlugConflict(portfolio);
        return Result.doNotRetry();
    }

    private void syncSlugConflict(Portfolio portfolio) {
        var slug = portfolio.getSpec().getSlug();
        var name = portfolio.getMetadata().getName();
        var conflicts = support.findSlugConflicts(slug, name);
        var status = portfolio.getStatus();
        if (status == null) {
            status = new Portfolio.PortfolioStatus();
            portfolio.setStatus(status);
        }
        var conflict = !conflicts.isEmpty();
        if (Objects.equals(status.getSlugConflict(), conflict)) {
            return;
        }
        status.setSlugConflict(conflict);
        portfolio.setStatus(status);
        client.update(portfolio);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Portfolio())
            .build();
    }
}
