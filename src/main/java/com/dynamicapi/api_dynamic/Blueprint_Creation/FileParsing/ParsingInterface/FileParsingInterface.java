package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface;

import java.io.InputStream;


public interface FileParsingInterface {
    String FileParser(InputStream dataStream);
    boolean supports(String type);
}
