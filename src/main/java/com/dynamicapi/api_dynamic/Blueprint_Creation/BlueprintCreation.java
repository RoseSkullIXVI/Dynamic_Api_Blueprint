package com.dynamicapi.api_dynamic.Blueprint_Creation;



import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingService.FileParsingService;
import com.dynamicapi.api_dynamic.Hadoop.HadoopService;


import org.json.JSONObject;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;

@Service
public class BlueprintCreation {
    private final KafkaReceiver<String, String> consumer;
    private static final Logger consumerLog = LoggerFactory.getLogger(BlueprintCreation.class);
    private final FileParsingService fileParsingService;
    private final HadoopService Hservice;

    public BlueprintCreation(KafkaReceiver<String, String> consumer, FileParsingService fileParsingService , HadoopService Hservice) {
        this.consumer = consumer;
        this.fileParsingService = fileParsingService;
        this.Hservice = Hservice;
    }
    @PostConstruct
    public void createBlueprint(){
       Flux<ReceiverRecord<String, String>> inboundFlux = consumer.receive();
       inboundFlux.subscribe(record -> {
       ReceiverOffset offset = record.receiverOffset();
       String base64 = record.value();
       try {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        String decodedString = new String(decodedBytes);
        byte[] bytesToSend = decodedString.getBytes();
        int contentLength = bytesToSend.length;
        InputStream inputStream = new ByteArrayInputStream(decodedString.getBytes());
        String filename = new String(record.headers().lastHeader("filename").value(), StandardCharsets.UTF_8);
        String keywords = fileParsingService.parseFile(inputStream,filename);
        String blueprint = getBlueprint(keywords, record, contentLength);
        Hservice.appendJsonStringToHdfsFile(blueprint,filename);
        Hservice.insertInputStream(inputStream, filename);
        System.out.println("Blueprint created" + blueprint );
       } catch (Exception e) {
        consumerLog.error("Error parsing file", e);
       }
        offset.acknowledge();             
    },
    error -> consumerLog.error("Error consuming messages", error),
    () -> consumerLog.info("Consumption complete")
    );
        
    }


    private String getBlueprint(String keywords, ReceiverRecord<String, String> record , int contentLength){
        JSONObject blueprint = new JSONObject();
        blueprint.put("keywords", keywords);
        blueprint.put("filename", new String(record.headers().lastHeader("filename").value(), StandardCharsets.UTF_8));
        blueprint.put("timestamp", new String(record.headers().lastHeader("timestamp").value(), StandardCharsets.UTF_8));
        blueprint.put("Velocity",new String(record.headers().lastHeader("Velocity").value(), StandardCharsets.UTF_8)); 
        blueprint.put("user-agent", new String(record.headers().lastHeader("user-agent").value(), StandardCharsets.UTF_8));
        blueprint.put("host", new String(record.headers().lastHeader("host").value(), StandardCharsets.UTF_8));
        String volume = contentVolume(contentLength);
        blueprint.put("volume", volume);
        blueprint.put("Type-of-Source", new String(record.headers().lastHeader("Type-of-Source").value(), StandardCharsets.UTF_8));
        blueprint.put("Type-of-Data", new String(record.headers().lastHeader("Type-of-Data").value(), StandardCharsets.UTF_8));
        blueprint.put("Value", new String(record.headers().lastHeader("Value").value(), StandardCharsets.UTF_8));
        blueprint.put("Veracity", new String(record.headers().lastHeader("Veracity").value(), StandardCharsets.UTF_8));
        return blueprint.toString();                
    } 

    private String contentVolume (int length){
        if(length < 1024){
            return length + "B";
        } else if(length < 1048576){
            return length/1024 + "KB";
        } else if (length < 1073741824){
            return length/1048576 + "MB";
        } else {
            return length/1073741824 + "GB";
        }
    }        
    
}
