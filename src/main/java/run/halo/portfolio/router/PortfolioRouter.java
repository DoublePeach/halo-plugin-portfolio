package run.halo.portfolio.router;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.theme.TemplateNameResolver;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class PortfolioRouter {

    private final TemplateNameResolver templateNameResolver;

    @Bean
    RouterFunction<ServerResponse> portfolioRoute() {
        return route(GET("/portfolio"), this::renderPortfolioPage);
    }

    private Mono<ServerResponse> renderPortfolioPage(ServerRequest request) {
        var model = new HashMap<String, Object>();
        model.put("title", "个人作品集");
        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "portfolio")
            .flatMap(templateName -> ServerResponse.ok().render(templateName, model));
    }
}
