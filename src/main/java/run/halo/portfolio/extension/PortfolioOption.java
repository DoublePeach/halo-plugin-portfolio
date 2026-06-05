package run.halo.portfolio.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
    group = "portfolio.plugin.halo.run",
    version = "v1alpha1",
    kind = "PortfolioOption",
    plural = "portfoliooptions",
    singular = "portfoliooption")
public class PortfolioOption extends AbstractExtension {

    public static final String TYPE_TECH_STACK = "TECH_STACK";
    public static final String TYPE_SOURCE = "SOURCE";
    public static final String TYPE_DOMAIN = "DOMAIN";

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Data
    @Schema(name = "PortfolioOptionSpec")
    public static class Spec {

        @Schema(requiredMode = REQUIRED)
        private String portfolioName;

        @Schema(requiredMode = REQUIRED, description = "TECH_STACK | SOURCE | DOMAIN")
        private String type;

        @Schema(requiredMode = REQUIRED)
        private String value;

        @Schema(requiredMode = REQUIRED)
        private String label;

        @Schema(defaultValue = "0")
        private Integer sortOrder;
    }
}
