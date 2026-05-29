package run.halo.portfolio.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioProjectVo {

    private String name;

    private String title;

    private String summary;

    private String description;

    private String coverImage;

    private List<String> gallery;

    private List<String> tags;

    private String domain;

    private List<String> techStack;

    private String source;

    private String sourceDetail;

    private Boolean featured;

    private Integer priority;

    private Instant startDate;

    private Instant endDate;
}
