package com.dynamicapi.api_dynamic.Blueprint_Creation;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;

@Service
public class BlueprintCreation {
    private final KafkaReceiver<String, String> consumer;
    private static final Logger consumerLog = LoggerFactory.getLogger(BlueprintCreation.class);

    public BlueprintCreation(KafkaReceiver<String, String> consumer) {
        this.consumer = consumer;
    }
    @PostConstruct
    public void createBlueprint(){
       Flux<ReceiverRecord<String, String>> inboundFlux = consumer.receive();
       inboundFlux.subscribe(record -> {
        ReceiverOffset offset = record.receiverOffset();
        

                  
        offset.acknowledge();             
    },
    error -> consumerLog.error("Error consuming messages", error),
    () -> consumerLog.info("Consumption complete")
    );
        
    }
    
}
