package com.dynamicapi.api_dynamic.Routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dynamicapi.api_dynamic.Handlers.StreamHandler;

@Configuration
@EnableWebFlux
public class StreamRouter {

    @Bean
    public RouterFunction<ServerResponse> route(StreamHandler handler) {
        return RouterFunctions
        .route(RequestPredicates.POST("/stream").and(RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA)), handler::handleFile);
    }

}
