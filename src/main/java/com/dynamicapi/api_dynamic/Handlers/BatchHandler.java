package com.dynamicapi.api_dynamic.Handlers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    public Mono<ServerResponse> handleBatchFile(ServerRequest request) {
        // Extract all request headers and transform them into Kafka RecordHeader objects
        List<Header> requestHeaders = request.headers().asHttpHeaders().entrySet().stream()
                .flatMap(headerEntry -> headerEntry.getValue().stream()
                        .map(headerValue -> new RecordHeader(headerEntry.getKey(), headerValue.getBytes(StandardCharsets.UTF_8))))
                .collect(Collectors.toList());

        return request.multipartData().flatMap(multipart -> {
            List<FilePart> fileParts = multipart.get("file")
                    .stream()
                    .filter(part -> part instanceof FilePart)
                    .map(part -> (FilePart) part)
                    .collect(Collectors.toList());

            if (!fileParts.isEmpty()) {
                Flux<SenderRecord<String, String, String>> senderRecordFlux = Flux.fromIterable(fileParts)
                    .flatMap(filePart -> DataBufferUtils.join(filePart.content())
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            String base64 = Base64.getEncoder().encodeToString(bytes);
                            long timestamp = System.currentTimeMillis();

                            // Combine file-specific headers with request headers
                            List<Header> combinedHeaders = new ArrayList<>(requestHeaders);
                            combinedHeaders.add(new RecordHeader("filename", filePart.filename().getBytes(StandardCharsets.UTF_8)));
                            combinedHeaders.add(new RecordHeader("timestamp", Long.toString(timestamp).getBytes(StandardCharsets.UTF_8)));

                            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, null, timestamp, null, base64, combinedHeaders);
                            return SenderRecord.create(record, "Filename: " + filePart.filename());
                        }));

                return kafkaSender.send(senderRecordFlux)
                        .doOnError(e -> log.error("Send failed", e))
                        .then(ServerResponse.ok().bodyValue("Files uploaded successfully"));
            } else {
                return Mono.error(new RuntimeException("File parts are missing"));
            }
        });
    }
}