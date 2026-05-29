package run.halo.portfolio.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupedProjectsResponse {

    private String groupBy;

    private List<GroupSectionVo> sections;
}
