package com.dynamicapi.api_dynamic.Routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dynamicapi.api_dynamic.Handlers.BatchHandler;

@Configuration
@EnableWebFlux
public class BatchRouter {

    @Bean
    public RouterFunction<ServerResponse> BatchRoute(BatchHandler handler) {
        return RouterFunctions
        .route(RequestPredicates.POST("/batch").and(RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA)), handler::handleBatchFile);
    }
    
}
