// package com.dynamicapi.api_dynamic.Routers;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.reactive.config.EnableWebFlux;
// import org.springframework.web.reactive.function.server.RequestPredicates;
// import org.springframework.web.reactive.function.server.RouterFunction;
// import org.springframework.web.reactive.function.server.RouterFunctions;
// import org.springframework.web.reactive.function.server.ServerResponse;

// import com.dynamicapi.api_dynamic.Handlers.MergeHandler;


// @Configuration
// @EnableWebFlux
// public class MergeRouter {

//      @Bean
//     public RouterFunction<ServerResponse> MergerRouter(MergeHandler handler) {
//         return RouterFunctions
//         .route(RequestPredicates.POST("/merge"), handler::handleHDFSFiles);
//     }
    
// }
