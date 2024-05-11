package com.dynamicapi.api_dynamic.Routers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.dynamicapi.api_dynamic.Hadoop.HadoopService;

import reactor.core.publisher.Mono;

@Controller
public class MergeController {  
    private final HadoopService hadoopService;

    public MergeController(HadoopService hadoopService) {
        this.hadoopService = hadoopService;
    }

    @PostMapping("/merge")
    public Mono<ResponseEntity<List<String>>> importing(@ModelAttribute ImportingCommands commands){
        try {
           boolean mergedFilesSuccess = hadoopService.mergeBlueprintJsonFiles();
           if (mergedFilesSuccess){
            List<String> outputFilenames =  hadoopService.handleRequest(commands.getTags());
            return Mono.just(ResponseEntity.ok().body(outputFilenames));
           }else 
           {
            throw new Exception("Failed to merge files.");
           }            
           
        } catch (Exception e) {
            return Mono.just(ResponseEntity.badRequest().body(List.of("Failed to merge files: " + e.getMessage())));
        }
    }
}
