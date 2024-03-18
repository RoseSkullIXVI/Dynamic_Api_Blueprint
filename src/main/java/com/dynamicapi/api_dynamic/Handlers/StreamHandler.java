package com.dynamicapi.api_dynamic.Handlers;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Component
public class StreamHandler {
    public Mono<ServerResponse> handleFile(ServerRequest request) {
        return request.multipartData().flatMap(multipart -> {
            FilePart filePart = (FilePart) multipart.toSingleValueMap().get("file");

            if (filePart != null) {
                return filePart.content()
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer); // Releasing the dataBuffer
                            return Base64.getEncoder().encodeToString(bytes); // Encoding to Base64
                        })
                        .collectList() // Collecting all parts into a list
                        .flatMap(encodedStrings -> {
                            String combinedString = String.join("", encodedStrings); // Combining all encoded parts
                            // Log or process the combined encoded string
                            System.out.println("Encoded File String: " + combinedString);
                            // Returning the response as Mono<ServerResponse>
                            return ServerResponse.ok().bodyValue("File uploaded successfully");
                        });
            } else {
                // Handling the case where file part is missing or not found in the request
                return Mono.error(new RuntimeException("File part is missing"));
            }
        });
    }
    
}
