package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.springframework.stereotype.Component;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@Component
public class CSVParser implements FileParsingInterface{
    @Override
    public String FileParser(InputStream file) {
      String headerJson = "";
      try(Reader reader = new InputStreamReader(file)){
      CSVReader csvReader = new CSVReaderBuilder(reader).build(); 
      String[] header = csvReader.readNext();
      Gson gson = new GsonBuilder().create();
      headerJson = gson.toJson(header);
      }
      catch (Exception e) {
         e.printStackTrace();
         headerJson = "Error parsing file";
      }
      return headerJson;
      
    }

    @Override
    public boolean supports(String type) {
       return type.equals("text/plain");
    }
    
    
}
