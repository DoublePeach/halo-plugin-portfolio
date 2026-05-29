package run.halo.portfolio;

import java.time.Instant;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import run.halo.portfolio.extension.PortfolioProject;

@Slf4j
@Component
public class PortfolioPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public PortfolioPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(PortfolioProject.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<PortfolioProject, Boolean>single("spec.featured", Boolean.class)
                .indexFunc(project -> project.getSpec().getFeatured()));
            indexSpecs.add(IndexSpecs.<PortfolioProject, Boolean>single("spec.published", Boolean.class)
                .indexFunc(project -> project.getSpec().getPublished()));
            indexSpecs.add(IndexSpecs.<PortfolioProject, Instant>single("spec.startDate", Instant.class)
                .indexFunc(project -> project.getSpec().getStartDate()));
            indexSpecs.add(IndexSpecs.<PortfolioProject, String>single("spec.domain", String.class)
                .indexFunc(project -> project.getSpec().getDomain()));
            indexSpecs.add(IndexSpecs.<PortfolioProject, String>single("spec.source", String.class)
                .indexFunc(project -> project.getSpec().getSource()));
            indexSpecs.add(IndexSpecs.<PortfolioProject, Integer>single("spec.priority", Integer.class)
                .indexFunc(project -> project.getSpec().getPriority()));
            indexSpecs.add(IndexSpecs.<PortfolioProject, String>multi("spec.techStack", String.class)
                .indexFunc(project -> {
                    var techStack = project.getSpec().getTechStack();
                    return techStack == null ? Set.of() : Set.copyOf(techStack);
                }));
            indexSpecs.add(IndexSpecs.<PortfolioProject, String>multi("spec.tags", String.class)
                .indexFunc(project -> {
                    var tags = project.getSpec().getTags();
                    return tags == null ? Set.of() : Set.copyOf(tags);
                }));
        });
        log.info("Portfolio plugin started.");
    }

    @Override
    public void stop() {
        Scheme scheme = schemeManager.get(PortfolioProject.class);
        schemeManager.unregister(scheme);
        log.info("Portfolio plugin stopped.");
    }
}
