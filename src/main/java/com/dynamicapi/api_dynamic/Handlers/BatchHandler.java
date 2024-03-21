package com.dynamicapi.api_dynamic.Handlers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
@Component
public class BatchHandler {
    private final KafkaSender<String, String> kafkaSender;
    private static final String TOPIC = "batch-topic";
    private static final Logger log = LoggerFactory.getLogger(BatchHandler.class);

    public BatchHandler(KafkaSender<String, String> kafkaSender) {
        this.kafkaSender = kafkaSender;
    }
    
    public Mono<ServerResponse> handleBatchFile(ServerRequest request){
         return request.multipartData().flatMap(multipart -> {
            // Process all "file" parts from the request
            List<FilePart> fileParts = multipart.get("file")
                    .stream()
                    .filter(part -> part instanceof FilePart)
                    .map(part -> (FilePart) part)
                    .collect(Collectors.toList());

            if (fileParts != null && !fileParts.isEmpty()) {
                // Create a Flux from the list of FileParts
                Flux<SenderRecord<String, String, String>> senderRecordFlux = Flux.fromIterable(fileParts)
                    .flatMap(filePart -> DataBufferUtils.join(filePart.content())
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            String base64 = Base64.getEncoder().encodeToString(bytes);
                            long timestamp = System.currentTimeMillis();
                            String metadata = "Filename: " + filePart.filename();

                            List<Header> headers = List.of(
                                new RecordHeader("filename", filePart.filename().getBytes(StandardCharsets.UTF_8)),
                                new RecordHeader("timestamp", Long.toString(timestamp).getBytes(StandardCharsets.UTF_8))
                            );
                            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, null, timestamp, null, base64, headers);
                            return SenderRecord.create(record, metadata);
                        }));

                // Send all records created from the files
                return kafkaSender.send(senderRecordFlux)
                        .doOnError(e -> log.error("Send failed", e))
                        .then(ServerResponse.ok().bodyValue("Files uploaded successfully"));
            } else {
                // Handling the case where file parts are missing or not found in the request
                return Mono.error(new RuntimeException("File parts are missing"));
            }
        });

    }
}
