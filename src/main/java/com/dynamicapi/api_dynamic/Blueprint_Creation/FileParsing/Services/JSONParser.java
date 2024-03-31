package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.InputStream;

import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;


import org.springframework.stereotype.Component;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JSONParser implements FileParsingInterface{
    Set<String> keys = new HashSet<>();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String FileParser(InputStream file) {
        try{
            JsonNode node = mapper.readTree(file);
            extractKeys(node,keys);
        } catch (Exception e) {
            e.printStackTrace();
            keys.add("Error parsing file");
        }
       return keys.toString();
    }

    private void extractKeys(JsonNode node, Set<String> keys) {
        if (node.isObject()) {
            Iterator<String> iterator = node.fieldNames();
            while (iterator.hasNext()) {
                String key = iterator.next();
                keys.add(key);
                extractKeys(node.get(key), keys);
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                extractKeys(arrayItem, keys);
            }
        }
    }

    @Override
    public boolean supports(String type) {
        return type.equals("application/json");
    }
    
}
