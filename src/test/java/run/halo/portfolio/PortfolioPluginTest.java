package run.halo.portfolio;

import org.junit.jupiter.api.Test;
import run.halo.portfolio.extension.Portfolio;
import run.halo.portfolio.extension.PortfolioOption;
import run.halo.portfolio.extension.PortfolioProject;

class PortfolioPluginTest {

    @Test
    void extensionModelsShouldExposeStatusFields() {
        var portfolio = new Portfolio();
        portfolio.setStatus(new Portfolio.PortfolioStatus());
        portfolio.getStatus().setSlugConflict(false);
        portfolio.getStatus().setProjectCount(0);

        var project = new PortfolioProject();
        project.setStatus(new PortfolioProject.ProjectStatus());
        project.getStatus().setInvalidPortfolio(false);

        var option = new PortfolioOption();
        option.setStatus(new PortfolioOption.OptionStatus());
        option.getStatus().setInvalidType(false);
        option.getStatus().setInvalidPortfolio(false);
        option.getStatus().setDuplicateValue(false);
    }
}
