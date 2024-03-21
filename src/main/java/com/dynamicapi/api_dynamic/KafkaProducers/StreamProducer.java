package com.dynamicapi.api_dynamic.KafkaProducers;


import java.util.Map;
import java.util.HashMap;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class StreamProducer {
    private static final String BOOTSTRAP_SERVERS = "kafka:9092";

    @Bean
    public KafkaSender<String,String> stream() {
       Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        SenderOptions<String, String> senderOptions = SenderOptions.create(props); 
        return KafkaSender.create(senderOptions);

    }
}
