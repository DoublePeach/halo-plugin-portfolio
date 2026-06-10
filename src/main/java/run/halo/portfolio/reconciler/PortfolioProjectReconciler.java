package run.halo.portfolio.reconciler;

import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.portfolio.extension.Portfolio;
import run.halo.portfolio.extension.PortfolioProject;
import run.halo.portfolio.support.PortfolioExtensionSupport;

@Component
@RequiredArgsConstructor
public class PortfolioProjectReconciler implements Reconciler<Reconciler.Request> {

    static final String FINALIZER = "portfolio.plugin.halo.run/project-cleanup";

    private final ExtensionClient client;
    private final PortfolioExtensionSupport support;

    @Override
    public Result reconcile(Request request) {
        var projectOpt = client.fetch(PortfolioProject.class, request.name());
        if (projectOpt.isEmpty()) {
            return Result.doNotRetry();
        }

        var project = projectOpt.get();
        var metadata = project.getMetadata();
        var finalizers = support.finalizersOf(metadata);

        if (!finalizers.contains(FINALIZER)) {
            support.addFinalizer(project, FINALIZER);
            return new Result(true, Duration.ofMillis(100));
        }

        syncPortfolioReference(project);

        var portfolioName = project.getSpec().getPortfolioName();
        if (metadata.getDeletionTimestamp() != null) {
            if (StringUtils.hasText(portfolioName)) {
                updateProjectCount(portfolioName);
            }
            support.removeFinalizer(project, FINALIZER);
            return Result.doNotRetry();
        }

        if (StringUtils.hasText(portfolioName)) {
            updateProjectCount(portfolioName);
        }
        return Result.doNotRetry();
    }

    private void syncPortfolioReference(PortfolioProject project) {
        var portfolioName = project.getSpec().getPortfolioName();
        var valid = support.portfolioExists(portfolioName);
        var status = project.getStatus();
        if (status == null) {
            status = new PortfolioProject.ProjectStatus();
            project.setStatus(status);
        }
        if (Objects.equals(status.getInvalidPortfolio(), !valid)) {
            return;
        }
        status.setInvalidPortfolio(!valid);
        project.setStatus(status);
        client.update(project);
    }

    private void updateProjectCount(String portfolioName) {
        client.fetch(Portfolio.class, portfolioName).ifPresent(portfolio -> {
            if (!support.isActive(portfolio.getMetadata())) {
                return;
            }
            var count = support.countActiveProjects(portfolioName);
            var status = portfolio.getStatus();
            if (status == null) {
                status = new Portfolio.PortfolioStatus();
                portfolio.setStatus(status);
            }
            if (Objects.equals(status.getProjectCount(), count)) {
                return;
            }
            status.setProjectCount(count);
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
