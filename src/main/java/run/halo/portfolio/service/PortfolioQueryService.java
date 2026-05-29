package run.halo.portfolio.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import static run.halo.app.extension.index.query.Queries.equal;

import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.portfolio.extension.PortfolioProject;
import run.halo.portfolio.model.GroupSectionVo;
import run.halo.portfolio.model.GroupedProjectsResponse;
import run.halo.portfolio.model.PortfolioProjectVo;
import run.halo.portfolio.model.TimelineGroupVo;
import run.halo.portfolio.model.TimelineResponse;

@Service
@RequiredArgsConstructor
public class PortfolioQueryService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ReactiveExtensionClient client;

    public Mono<List<PortfolioProjectVo>> listFeatured() {
        return listPublishedProjects()
            .map(projects -> projects.stream()
                .filter(p -> Boolean.TRUE.equals(p.getFeatured()))
                .sorted(Comparator
                    .comparing(PortfolioProjectVo::getPriority,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProjectVo::getStartDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList());
    }

    public Mono<List<PortfolioProjectVo>> listProjects(String domain, String source, String techStack) {
        return listPublishedProjects()
            .map(projects -> projects.stream()
                .filter(p -> !StringUtils.hasText(domain) || domain.equals(p.getDomain()))
                .filter(p -> !StringUtils.hasText(source) || source.equals(p.getSource()))
                .filter(p -> !StringUtils.hasText(techStack)
                    || (p.getTechStack() != null && p.getTechStack().contains(techStack)))
                .sorted(defaultComparator())
                .toList());
    }

    public Mono<TimelineResponse> listTimeline(String granularity, String order) {
        var isAsc = "asc".equalsIgnoreCase(order);
        return listPublishedProjects()
            .map(projects -> {
                var formatter = "month".equalsIgnoreCase(granularity) ? MONTH_FORMAT : YEAR_FORMAT;
                var grouped = new LinkedHashMap<String, List<PortfolioProjectVo>>();
                projects.stream()
                    .sorted(defaultComparator())
                    .forEach(project -> {
                        var key = formatDateKey(project.getStartDate(), formatter);
                        grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(project);
                    });

                var groups = grouped.entrySet().stream()
                    .map(entry -> TimelineGroupVo.builder()
                        .key(entry.getKey())
                        .label(entry.getKey())
                        .projects(entry.getValue())
                        .build())
                    .sorted(isAsc
                        ? Comparator.comparing(TimelineGroupVo::getKey)
                        : Comparator.comparing(TimelineGroupVo::getKey).reversed())
                    .toList();

                return TimelineResponse.builder()
                    .granularity("month".equalsIgnoreCase(granularity) ? "month" : "year")
                    .order(isAsc ? "asc" : "desc")
                    .groups(groups)
                    .build();
            });
    }

    public Mono<GroupedProjectsResponse> listGrouped(String groupBy) {
        return listPublishedProjects()
            .map(projects -> {
                var sections = switch (groupBy) {
                    case "techStack" -> groupByTechStack(projects);
                    case "source" -> groupByField(projects, PortfolioProjectVo::getSource, this::sourceLabel);
                    default -> groupByField(projects, PortfolioProjectVo::getDomain, this::domainLabel);
                };
                return GroupedProjectsResponse.builder()
                    .groupBy(groupBy)
                    .sections(sections)
                    .build();
            });
    }

    public Mono<PortfolioProjectVo> getProject(String name) {
        return client.fetch(PortfolioProject.class, name)
            .filter(project -> Boolean.TRUE.equals(project.getSpec().getPublished()))
            .map(this::toVo);
    }

    private Mono<List<PortfolioProjectVo>> listPublishedProjects() {
        var listOptions = ListOptions.builder()
            .fieldQuery(equal("spec.published", true))
            .build();

        return client.listBy(PortfolioProject.class, listOptions,
                PageRequestImpl.of(1, 500, Sort.by("spec.startDate").descending()))
            .map(ListResult::getItems)
            .map(items -> items.stream()
                .map(this::toVo)
                .sorted(defaultComparator())
                .toList());
    }

    private List<GroupSectionVo> groupByField(
        List<PortfolioProjectVo> projects,
        java.util.function.Function<PortfolioProjectVo, String> keyExtractor,
        java.util.function.Function<String, String> labelMapper) {
        Map<String, List<PortfolioProjectVo>> grouped = projects.stream()
            .collect(Collectors.groupingBy(
                p -> {
                    var key = keyExtractor.apply(p);
                    return StringUtils.hasText(key) ? key : "other";
                },
                LinkedHashMap::new,
                Collectors.toList()));

        return grouped.entrySet().stream()
            .map(entry -> GroupSectionVo.builder()
                .key(entry.getKey())
                .label(labelMapper.apply(entry.getKey()))
                .projects(entry.getValue().stream().sorted(defaultComparator()).toList())
                .build())
            .sorted(Comparator.comparing(GroupSectionVo::getKey))
            .toList();
    }

    private List<GroupSectionVo> groupByTechStack(List<PortfolioProjectVo> projects) {
        Map<String, List<PortfolioProjectVo>> grouped = new LinkedHashMap<>();
        projects.forEach(project -> {
            var stacks = project.getTechStack();
            if (stacks == null || stacks.isEmpty()) {
                grouped.computeIfAbsent("other", k -> new ArrayList<>()).add(project);
                return;
            }
            stacks.forEach(stack -> grouped.computeIfAbsent(stack, k -> new ArrayList<>()).add(project));
        });

        return grouped.entrySet().stream()
            .map(entry -> GroupSectionVo.builder()
                .key(entry.getKey())
                .label(entry.getKey())
                .projects(entry.getValue().stream()
                    .distinct()
                    .sorted(defaultComparator())
                    .toList())
                .build())
            .sorted(Comparator.comparing(GroupSectionVo::getKey))
            .toList();
    }

    private Comparator<PortfolioProjectVo> defaultComparator() {
        return Comparator
            .comparing(PortfolioProjectVo::getStartDate, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(PortfolioProjectVo::getPriority, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private String formatDateKey(Instant instant, DateTimeFormatter formatter) {
        if (instant == null) {
            return "未标注时间";
        }
        return formatter.format(instant.atZone(ZONE));
    }

    private PortfolioProjectVo toVo(PortfolioProject project) {
        var spec = project.getSpec();
        return PortfolioProjectVo.builder()
            .name(project.getMetadata().getName())
            .title(spec.getTitle())
            .summary(spec.getSummary())
            .description(spec.getDescription())
            .coverImage(spec.getCoverImage())
            .gallery(spec.getGallery())
            .tags(spec.getTags())
            .domain(spec.getDomain())
            .techStack(spec.getTechStack())
            .source(spec.getSource())
            .sourceDetail(spec.getSourceDetail())
            .featured(spec.getFeatured())
            .priority(spec.getPriority())
            .startDate(spec.getStartDate())
            .endDate(spec.getEndDate())
            .build();
    }

    private String domainLabel(String key) {
        return switch (Objects.requireNonNullElse(key, "other")) {
            case "java" -> "Java 传统项目";
            case "middleware" -> "中间件轮子";
            case "ai-vibe" -> "VibeCoding 项目";
            case "ai-rag" -> "RAG 项目";
            case "ai-agent" -> "Agent 项目";
            case "ai-enterprise" -> "企业 AI 能力接入";
            default -> "其他领域";
        };
    }

    private String sourceLabel(String key) {
        return switch (Objects.requireNonNullElse(key, "other")) {
            case "university" -> "大学在校期间";
            case "intern" -> "大学实习期间";
            case "company" -> "企业项目";
            case "oss" -> "开源项目参与";
            default -> "其他来源";
        };
    }
}
