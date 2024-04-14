package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import java.util.Set;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JSONParser implements FileParsingInterface{
    Set<String> keys = new HashSet<>();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String FileParser(InputStream file) {
        Set<String> finalKeys = new HashSet<>();
        try {
            // Convert the MultipartFile's input stream to JSONObject
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject rootObject = new JSONObject(sb.toString());
            
            // Extract keys
            findAllKeys(rootObject, finalKeys);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing file: " + e.getMessage();
        }
        
        // Return a string representation of all the keys
        return finalKeys.toString();
    
    }

     private void findAllKeys(Object object, Set<String> finalKeys) {
        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            jsonObject.keySet().forEach(childKey -> {
                finalKeys.add(childKey);
                findAllKeys(jsonObject.get(childKey), finalKeys);
            });
        } else if (object instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) object;
            IntStream.range(0, jsonArray.length())
                    .mapToObj(jsonArray::get)
                    .forEach(o -> findAllKeys(o, finalKeys));
        }
    }

    @Override
    public boolean supports(String type) {
        return type.equals("application/json");
    }
    
}
