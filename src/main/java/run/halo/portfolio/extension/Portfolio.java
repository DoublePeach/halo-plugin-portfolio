package run.halo.portfolio.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "portfolio.plugin.halo.run", version = "v1alpha1", kind = "Portfolio",
    plural = "portfolios", singular = "portfolio")
public class Portfolio extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private PortfolioSpec spec;

    @Schema
    private PortfolioStatus status;

    @Data
    public static class PortfolioSpec {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        @Schema(requiredMode = REQUIRED, pattern = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$")
        private String slug;

        @Schema
        private String description;

        @Schema
        private String cover;

        @Schema(defaultValue = "true")
        private Boolean publicView;

        @Schema(defaultValue = "0")
        private Integer priority;
    }

    @Data
    public static class PortfolioStatus {
        @Schema(defaultValue = "0")
        private Integer projectCount;

        @Schema(description = "True when another portfolio uses the same slug")
        private Boolean slugConflict;
    }
}
