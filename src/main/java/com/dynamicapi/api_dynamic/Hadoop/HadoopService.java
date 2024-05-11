package com.dynamicapi.api_dynamic.Hadoop;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Component;
import org.apache.hadoop.io.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;


import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;





@Component
public class HadoopService {

    private FileSystem fileSystem;
    public HadoopService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void appendJsonStringToHdfsFile(String jsonString, String filename) throws IOException {
        Path path = new Path("hdfs://roseskull:8020/blueprint/input/"+filename+"_blueprint.json");
        Path parentDir = path.getParent();
        FSDataOutputStream output = null;

        if (!fileSystem.exists(parentDir)) {
            fileSystem.mkdirs(parentDir);
            System.out.println("Directory created: " + parentDir);
        }
        try {
            output = fileSystem.create(path);
            output.writeChars(jsonString);
            System.out.println("File created");
            output.close();
        } catch (Exception e) {
            System.out.println("Error creating file: " + e.getMessage());
        }
    }
    public void insertInputStream (InputStream input, String filename) throws IOException{
        String timeStamp = new SimpleDateFormat("ddMMyyyy").format(new Date());
        Path path = new Path("hdfs://roseskull:8020/blueprint/input/"+timeStamp+"_"+filename);
        Path parentDir = path.getParent();

        if (!fileSystem.exists(parentDir)) {
            fileSystem.mkdirs(parentDir);
            System.out.println("Directory created: " + parentDir);
        }
        try{
            FSDataOutputStream output = fileSystem.create(path);
            IOUtils.copyBytes(input, output, fileSystem.getConf());  
            System.out.println("File writen to HDFS"); 
        }catch (Exception e)
        {
            System.out.println("Error creating file: " + e.getMessage());
        }       
        
    }

     public Boolean mergeBlueprintJsonFiles() throws IOException {
        Path directory = new Path("hdfs://roseskull:8020/blueprint/input");
        Path jsonFilePath = new Path("hdfs://roseskull:8020/blueprint/input/merged_blueprint.json");
        if (fileSystem.exists(jsonFilePath)) {
            if( fileSystem.delete(jsonFilePath, false)){
                 System.out.println("deleting merged file");
            }
        }
        FileStatus[] fileStatuses = fileSystem.listStatus(directory);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode allJsons = mapper.createArrayNode();

        for (FileStatus fileStatus : fileStatuses) {
            if (!fileSystem.exists(jsonFilePath)){           
            if (fileStatus.getPath().getName().contains("blueprint") && !fileStatus.getPath().getName().contains("grouped")) {
                try (FSDataInputStream fsDataInputStream = fileSystem.open(fileStatus.getPath());
                     BufferedReader br = new BufferedReader(new InputStreamReader(fsDataInputStream))) {
                    String jsonContent = br.lines().collect(Collectors.joining());
                    jsonContent = jsonContent.replaceAll("[\\x00-\\x08\\x0E-\\x1F]", ""); // Clean illegal chars
                    allJsons.add(mapper.readTree(jsonContent));
                } catch (Exception e) {
                    System.err.println("Error processing file " + fileStatus.getPath() + ": " + e.getMessage());
                    return false;
                }
            }
        }
        }

        String mergedJson = mapper.writeValueAsString(allJsons);
        appendJsonStringToHdfsFile(mergedJson, "merged");
        return true;
    }

    public List<String> handleRequest(String request) throws IOException {
        if (request == null || request.isEmpty()) {
            throw new IOException("Tags parameter is missing");
        }
        String[] tags = request.split("\\s+");
        List<String> outputFilenames = new ArrayList<>();
    
        // Update the path to a corrected file location
        Path jsonFilePath = new Path("hdfs://roseskull:8020/blueprint/input/merged_blueprint.json");
        if (!fileSystem.exists(jsonFilePath)) {
            throw new IOException("Merged JSON file not found at path: " + jsonFilePath);
        }
    
        // Use FSDataInputStream to read directly from HDFS
        try (FSDataInputStream inputStream = fileSystem.open(jsonFilePath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String content = reader.lines().collect(Collectors.joining());
            String jsonContent = content.replaceAll("[\\x00-\\x08\\x0E-\\x1F]", ""); // Clean illegal chars
            JSONArray jsonArray = new JSONArray(jsonContent);
    
            // Pass the data to processJsonData for further processing
            outputFilenames = processAndGroupJsonData(jsonArray, tags);
            return outputFilenames;
        } catch (Exception e) {
            System.err.println("Error reading merged blueprint JSON file: " + e.getMessage());
            throw e; // Re-throw to let the caller handle it
        }
    }
  // Groups JSON data based on dynamic tags and writes grouped JSON files to HDFS
  private List<String> processAndGroupJsonData(JSONArray jsonArray, String[] tags) throws IOException {
    Map<String, List<JSONObject>> groupedData = new HashMap<>();
    List<String> outputFilenames = new ArrayList<>();

    // Group JSON objects by specified tags
    for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        String key = Arrays.stream(tags)
                .map(tag -> jsonObject.optString(tag, "unknown"))
                .collect(Collectors.joining("-"));

        groupedData.computeIfAbsent(key, k -> new ArrayList<>()).add(jsonObject);
    }

    // Write grouped data to separate HDFS files
    for (Map.Entry<String, List<JSONObject>> entry : groupedData.entrySet()) {
        String groupKey = entry.getKey();
        String outputFilename = groupKey.replaceAll("[^a-zA-Z0-9-]", "_") + "_grouped";
        String jsonString = new JSONArray(entry.getValue()).toString(2);

        appendJsonStringToHdfsFile(jsonString, outputFilename);
        outputFilenames.add("hdfs://roseskull:8020/blueprint/input/" + outputFilename );
    }

    return outputFilenames;
}

}
