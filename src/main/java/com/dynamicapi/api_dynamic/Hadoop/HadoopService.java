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

     public void mergeBlueprintJsonFiles() throws IOException {
        Path directory = new Path("hdfs://roseskull:8020/blueprint/input");
        FileStatus[] fileStatuses = fileSystem.listStatus(directory);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode allJsons = mapper.createArrayNode();

        for (FileStatus fileStatus : fileStatuses) {
            if (fileStatus.getPath().getName().contains("blueprint")) {
                try (FSDataInputStream fsDataInputStream = fileSystem.open(fileStatus.getPath());
                     BufferedReader br = new BufferedReader(new InputStreamReader(fsDataInputStream))) {
                    String jsonContent = br.lines().collect(Collectors.joining());
                    jsonContent = jsonContent.replaceAll("[\\x00-\\x08\\x0E-\\x1F]", ""); // Clean illegal chars
                    allJsons.add(mapper.readTree(jsonContent));
                } catch (Exception e) {
                    System.err.println("Error processing file " + fileStatus.getPath() + ": " + e.getMessage());
                }
            }
        }
        String mergedJson = mapper.writeValueAsString(allJsons);
        appendJsonStringToHdfsFile(mergedJson, "merged_blueprint.json");
    }



}
