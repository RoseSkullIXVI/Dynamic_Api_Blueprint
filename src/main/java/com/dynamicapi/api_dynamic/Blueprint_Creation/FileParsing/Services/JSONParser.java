package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JSONParser implements FileParsingInterface{
    List<String> keys = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String FileParser(InputStream file) {
        try(InputStream is = file) {
            JsonNode rootNode = mapper.readTree(is);
            if (rootNode.isObject()) {
                Iterator<String> iterator = rootNode.fieldNames();
                iterator.forEachRemaining(e -> keys.add(e));
            } else if (rootNode.isArray()) {
                for (JsonNode arrayItem : rootNode) {
                    if (arrayItem.isObject()) {
                        Iterator<String> iterator = arrayItem.fieldNames();
                        iterator.forEachRemaining(e -> keys.add(e));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            keys.add("Error parsing file");
        }
       return keys.toString();
    }

    @Override
    public boolean supports(String type) {
        return type.equals("application/json");
    }
    
}
