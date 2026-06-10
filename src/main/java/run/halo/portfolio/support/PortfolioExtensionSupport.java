package run.halo.portfolio.support;

import static run.halo.app.extension.index.query.Queries.equal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.PageRequestImpl;
import run.halo.portfolio.extension.Portfolio;
import run.halo.portfolio.extension.PortfolioOption;
import run.halo.portfolio.extension.PortfolioProject;

@Component
@RequiredArgsConstructor
public class PortfolioExtensionSupport {

    private static final int LIST_PAGE_SIZE = 500;

    private final ExtensionClient client;

    public boolean isActive(MetadataOperator metadata) {
        return metadata != null && metadata.getDeletionTimestamp() == null;
    }

    public boolean portfolioExists(String portfolioName) {
        if (!StringUtils.hasText(portfolioName)) {
            return false;
        }
        return client.fetch(Portfolio.class, portfolioName)
            .filter(portfolio -> isActive(portfolio.getMetadata()))
            .isPresent();
    }

    public int countActiveProjects(String portfolioName) {
        return listActiveProjects(portfolioName).size();
    }

    public int countActiveOptions(String portfolioName) {
        return listActiveOptions(portfolioName).size();
    }

    public boolean hasPortfolioChildren(String portfolioName) {
        return countActiveProjects(portfolioName) > 0 || countActiveOptions(portfolioName) > 0;
    }

    public List<PortfolioProject> listActiveProjects(String portfolioName) {
        var listOptions = ListOptions.builder()
            .fieldQuery(equal("spec.portfolioName", portfolioName))
            .build();
        return client.listBy(PortfolioProject.class, listOptions,
                PageRequestImpl.of(1, LIST_PAGE_SIZE, Sort.unsorted()))
            .getItems()
            .stream()
            .filter(project -> isActive(project.getMetadata()))
            .toList();
    }

    public List<PortfolioOption> listActiveOptions(String portfolioName) {
        var listOptions = ListOptions.builder()
            .fieldQuery(equal("spec.portfolioName", portfolioName))
            .build();
        return client.listBy(PortfolioOption.class, listOptions,
                PageRequestImpl.of(1, LIST_PAGE_SIZE, Sort.unsorted()))
            .getItems()
            .stream()
            .filter(option -> isActive(option.getMetadata()))
            .toList();
    }

    public List<Portfolio> findSlugConflicts(String slug, String excludeName) {
        if (!StringUtils.hasText(slug)) {
            return List.of();
        }
        var listOptions = ListOptions.builder()
            .fieldQuery(equal("spec.slug", slug))
            .build();
        return client.listBy(Portfolio.class, listOptions,
                PageRequestImpl.of(1, LIST_PAGE_SIZE, Sort.unsorted()))
            .getItems()
            .stream()
            .filter(portfolio -> isActive(portfolio.getMetadata()))
            .filter(portfolio -> !portfolio.getMetadata().getName().equals(excludeName))
            .toList();
    }

    public List<PortfolioOption> findOptionValueConflicts(
        String portfolioName,
        String type,
        String value,
        String excludeName
    ) {
        if (!StringUtils.hasText(portfolioName) || !StringUtils.hasText(type) || !StringUtils.hasText(value)) {
            return List.of();
        }
        return listActiveOptions(portfolioName).stream()
            .filter(option -> type.equals(option.getSpec().getType()))
            .filter(option -> value.equals(option.getSpec().getValue()))
            .filter(option -> !option.getMetadata().getName().equals(excludeName))
            .toList();
    }

    public Set<String> finalizersOf(MetadataOperator metadata) {
        var finalizers = metadata.getFinalizers();
        if (finalizers == null || finalizers.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(finalizers);
    }

    public boolean addFinalizer(Extension extension, String finalizer) {
        var metadata = extension.getMetadata();
        var finalizers = new HashSet<>(finalizersOf(metadata));
        if (finalizers.contains(finalizer)) {
            return false;
        }
        finalizers.add(finalizer);
        metadata.setFinalizers(finalizers);
        client.update(extension);
        return true;
    }

    public void removeFinalizer(Extension extension, String finalizer) {
        var metadata = extension.getMetadata();
        var finalizers = new HashSet<>(finalizersOf(metadata));
        if (!finalizers.remove(finalizer)) {
            return;
        }
        metadata.setFinalizers(finalizers.isEmpty() ? null : finalizers);
        client.update(extension);
    }
}
