package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;
import org.apache.tika.Tika;


@Service
@Async
public class FileParsingService {
    private final List<FileParsingInterface> fileParsingService;

    public FileParsingService(List<FileParsingInterface> fileParsingService) {
        this.fileParsingService = fileParsingService;
    }

    public String parseFile(InputStream data, String filename) {
        Tika tika = new Tika();
        String type;
        try {
            type = tika.detect(data, filename);
        } catch (IOException e) {
            throw new RuntimeException("Error detecting file type", e);
        }
        for (FileParsingInterface fileParsingService : fileParsingService) {
            if (fileParsingService.supports(type)) {
                return fileParsingService.FileParser(data);
            }
        }
        throw new IllegalArgumentException("Unsupported file type: " + type);
    }
    
}
