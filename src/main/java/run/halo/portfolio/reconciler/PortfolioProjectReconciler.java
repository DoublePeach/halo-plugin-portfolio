package run.halo.portfolio.reconciler;

import static run.halo.app.extension.index.query.Queries.equal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.portfolio.extension.Portfolio;
import run.halo.portfolio.extension.PortfolioProject;

@Component
@RequiredArgsConstructor
public class PortfolioProjectReconciler implements Reconciler<Reconciler.Request> {

    private final ExtensionClient client;

    @Override
    public Result reconcile(Request request) {
        client.fetch(PortfolioProject.class, request.name()).ifPresent(project -> {
            var portfolioName = project.getSpec().getPortfolioName();
            if (StringUtils.hasText(portfolioName)) {
                updateProjectCount(portfolioName);
            }
        });
        return Result.doNotRetry();
    }

    private void updateProjectCount(String portfolioName) {
        client.fetch(Portfolio.class, portfolioName).ifPresent(portfolio -> {
            var listOptions = ListOptions.builder()
                .andQuery(equal("spec.portfolioName", portfolioName))
                .build();
            long count = client.countBy(PortfolioProject.class, listOptions);
            var status = portfolio.getStatus();
            if (status == null) {
                status = new Portfolio.PortfolioStatus();
                portfolio.setStatus(status);
            }
            status.setProjectCount((int) count);
            client.update(portfolio);
        });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new PortfolioProject())
            .build();
    }
}
