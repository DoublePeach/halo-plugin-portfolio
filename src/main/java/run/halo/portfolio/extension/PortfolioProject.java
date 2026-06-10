package run.halo.portfolio.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
    group = "portfolio.plugin.halo.run",
    version = "v1alpha1",
    kind = "PortfolioProject",
    plural = "portfolioprojects",
    singular = "portfolioproject")
public class PortfolioProject extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Schema
    private ProjectStatus status;

    @Data
    public static class ProjectStatus {
        @Schema(description = "True when spec.portfolioName does not reference an existing portfolio")
        private Boolean invalidPortfolio;
    }

    @Data
    @Schema(name = "PortfolioProjectSpec")
    public static class Spec {

        @Schema(requiredMode = REQUIRED, maxLength = 200)
        private String title;

        @Schema(maxLength = 500)
        private String summary;

        private String description;

        private String coverImage;

        @ArraySchema(maxItems = 20)
        private List<String> gallery;

        @ArraySchema(maxItems = 20)
        private List<String> tags;

        private String domain;

        private List<String> techStack;

        private String source;

        private String sourceDetail;

        @Schema(defaultValue = "false")
        private Boolean featured;

        private Integer priority;

        private Instant startDate;

        private Instant endDate;

        @Schema(defaultValue = "false")
        private Boolean published;

        @Schema(requiredMode = REQUIRED, description = "Portfolio name this project belongs to")
        private String portfolioName;

        @Schema(description = "Linked Halo post name for project detail")
        private String postName;
    }
}
