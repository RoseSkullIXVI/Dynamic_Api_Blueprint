package com.dynamicapi.api_dynamic.Handlers;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StreamHandler {
    private final KafkaSender<String, String> kafkaSender;
    private static final String TOPIC = "stream-topic";
    private static final Logger log = LoggerFactory.getLogger(StreamHandler.class);

    public StreamHandler(KafkaSender<String, String> kafkaSender) {
        this.kafkaSender = kafkaSender;
    }
    

    public Mono<ServerResponse> handleFile(ServerRequest request) {
        List<Header> requestHeaders = request.headers().asHttpHeaders().entrySet().stream()
                .flatMap(headerEntry -> headerEntry.getValue().stream()
                        .map(headerValue -> new RecordHeader(headerEntry.getKey(), headerValue.getBytes(StandardCharsets.UTF_8))))
                .collect(Collectors.toList());

        return request.multipartData().flatMap(multipart -> {
            FilePart filePart = (FilePart) multipart.toSingleValueMap().get("file");

            if (filePart != null) {
                Flux<DataBuffer> content = filePart.content();
                
                Flux<SenderRecord<String, String, String>> streamData = content
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            String base64 = Base64.getEncoder().encodeToString(bytes);
                            long timestamp = System.currentTimeMillis();
                            String metadata = "Filename" + filePart.filename();

                            List<Header> combinedHeaders = new ArrayList<>(requestHeaders);
                            combinedHeaders.add(new RecordHeader("filename", filePart.filename().getBytes(StandardCharsets.UTF_8)));
                            combinedHeaders.add(new RecordHeader("timestamp", Long.toString(timestamp).getBytes(StandardCharsets.UTF_8)));

                            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, null, timestamp, null, base64,combinedHeaders);
                            return SenderRecord.create(record, metadata);
                        });

                        return kafkaSender.send(streamData)
                                .doOnError(e -> log.error("Send failed", e))
                                .then(ServerResponse.ok().bodyValue("File uploaded successfully"));
                        
            } else {
                // Handling the case where file part is missing or not found in the request
                return Mono.error(new RuntimeException("File part is missing"));
            }
        });
    }
    
}
