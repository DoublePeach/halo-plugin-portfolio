package run.halo.portfolio.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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

    @Data
    @Schema(name = "PortfolioProjectSpec")
    public static class Spec {

        @Schema(requiredMode = REQUIRED, maxLength = 200)
        private String title;

        @Schema(maxLength = 500)
        private String summary;

        private String description;

        private String coverImage;

        private List<String> gallery;

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
    }
}
