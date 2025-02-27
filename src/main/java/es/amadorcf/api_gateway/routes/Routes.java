package es.amadorcf.api_gateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class Routes {

    // Configuración de las URLs de los microservicios
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${task.service.url}")
    private String taskServiceUrl;

    // Configuración de rutas y circuit breakers

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute(){
        return route("user_service")
                .route(RequestPredicates.path("/api/users"), HandlerFunctions.http(userServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("userServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> taskServiceRoute(){
        return route("task_service")
                .route(RequestPredicates.path("/api/tasks"), HandlerFunctions.http(taskServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("taskServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    // Rutas para Swagger API Docs de los microservicios
    @Bean
    public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
        return route("user_service_swagger")
                .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"),
                        HandlerFunctions.http(userServiceUrl + "/v3/api-docs"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("userServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> taskServiceSwaggerRoute() {
        return route("task_service_swagger")
                .route(RequestPredicates.path("/aggregate/task-service/v3/api-docs"),
                        HandlerFunctions.http(taskServiceUrl + "/v3/api-docs"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("taskServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    // Ruta de fallback para servicios no disponibles
    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service Unavailable, please try again later"))
                .build();
    }
}
