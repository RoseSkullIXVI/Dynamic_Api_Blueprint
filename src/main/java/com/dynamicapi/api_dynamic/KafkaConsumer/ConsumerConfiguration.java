package com.dynamicapi.api_dynamic.KafkaConsumer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;


@Configuration
public class ConsumerConfiguration {
    private static final String boot_server = "kafka:9092"; 
    private static ArrayList<String> topicList = new ArrayList<>(Arrays.asList("stream-topic" , "batch-topic"));
    
    @Bean
    public KafkaReceiver<String,String> consumer() {
        Map<String, Object> ConsumerProps = new HashMap<>();
        ConsumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boot_server);
        ConsumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        ConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        ConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        ConsumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.<String, String>create(ConsumerProps).subscription(topicList);
        return KafkaReceiver.create(receiverOptions);

    }
}
