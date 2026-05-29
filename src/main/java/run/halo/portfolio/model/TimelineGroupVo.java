package run.halo.portfolio.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimelineGroupVo {

    private String key;

    private String label;

    private List<PortfolioProjectVo> projects;
}
