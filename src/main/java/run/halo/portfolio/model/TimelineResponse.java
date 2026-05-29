package run.halo.portfolio.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimelineResponse {

    private String granularity;

    private String order;

    private List<TimelineGroupVo> groups;
}
