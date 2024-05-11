// package com.dynamicapi.api_dynamic.Handlers;


// import javax.servlet.http.HttpServletRequest;

// import org.springframework.stereotype.Component;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.reactive.function.server.ServerRequest;
// import org.springframework.web.reactive.function.server.ServerResponse;

// import com.dynamicapi.api_dynamic.Hadoop.HadoopService;

// import reactor.core.publisher.Mono;
// @CrossOrigin
// @Component
// public class MergeHandler {
//     private HadoopService hadoopService;

//     public MergeHandler(HadoopService hadoopService) {
//         this.hadoopService = hadoopService;
//     }
//     public Mono<ServerResponse> handleHDFSFiles(ServerRequest request) {
//         try {
//             hadoopService.mergeBlueprintJsonFiles();
//             hadoopService.handleRequest(request);
//             return ServerResponse.ok().bodyValue("Files merged successfully.");
//         } catch (Exception e) {
//             return ServerResponse.badRequest().bodyValue("Failed to merge files: " + e.getMessage());
//         }
//     }
// }
