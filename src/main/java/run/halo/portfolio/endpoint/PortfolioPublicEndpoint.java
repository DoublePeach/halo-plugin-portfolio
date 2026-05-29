package run.halo.portfolio.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.portfolio.service.PortfolioQueryService;

@Component
@RequiredArgsConstructor
public class PortfolioPublicEndpoint implements CustomEndpoint {

    private static final String TAG = "PortfolioV1alpha1Public";

    private final PortfolioQueryService queryService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return SpringdocRouteBuilder.route()
            .GET("/projects/featured", this::listFeatured,
                builder -> builder.operationId("ListFeaturedProjects")
                    .description("List featured portfolio projects")
                    .tag(TAG)
                    .response(responseBuilder().implementationArray(Object.class)))
            .GET("/projects/timeline", this::listTimeline,
                builder -> builder.operationId("ListTimelineProjects")
                    .description("List projects grouped by timeline")
                    .tag(TAG)
                    .response(responseBuilder().implementation(Object.class)))
            .GET("/projects/grouped", this::listGrouped,
                builder -> builder.operationId("ListGroupedProjects")
                    .description("List projects grouped by dimension")
                    .tag(TAG)
                    .response(responseBuilder().implementation(Object.class)))
            .GET("/projects", this::listProjects,
                builder -> builder.operationId("ListPortfolioProjects")
                    .description("List portfolio projects with filters")
                    .tag(TAG)
                    .response(responseBuilder().implementationArray(Object.class)))
            .GET("/projects/{name}", this::getProject,
                builder -> builder.operationId("GetPortfolioProject")
                    .description("Get portfolio project detail")
                    .tag(TAG)
                    .response(responseBuilder().implementation(Object.class)))
            .build();
    }

    private Mono<ServerResponse> listFeatured(ServerRequest request) {
        return queryService.listFeatured()
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> listTimeline(ServerRequest request) {
        var granularity = request.queryParam("granularity").orElse("year");
        var order = request.queryParam("order").orElse("desc");
        return queryService.listTimeline(granularity, order)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> listGrouped(ServerRequest request) {
        var groupBy = request.queryParam("groupBy").orElse("domain");
        return queryService.listGrouped(groupBy)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> listProjects(ServerRequest request) {
        var domain = request.queryParam("domain").orElse(null);
        var source = request.queryParam("source").orElse(null);
        var techStack = request.queryParam("techStack").orElse(null);
        return queryService.listProjects(domain, source, techStack)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> getProject(ServerRequest request) {
        var name = request.pathVariable("name");
        return queryService.getProject(name)
            .flatMap(result -> ServerResponse.ok().bodyValue(result))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.portfolio.plugin.halo.run", "v1alpha1");
    }
}
