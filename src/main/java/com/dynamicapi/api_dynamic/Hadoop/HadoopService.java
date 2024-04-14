package com.dynamicapi.api_dynamic.Hadoop;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class HadoopService {
    
    
    private FileSystem fileSystem;

    public HadoopService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void appendJsonStringToHdfsFile(String jsonString) throws IOException {
        Path path = new Path("hdfs://roseskull:8020/user/hadoop/input/blueprint.json");
        if (!fileSystem.exists(path)) {
            try (FSDataOutputStream outputStream = fileSystem.create(path)) {
               System.out.println("File created");
            }catch (Exception e){
                System.out.println("Error creating file");
            }
        }
        
        // Convert the JSON string to a byte array input stream.
        try (InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes())) {
            // Open the file for appending.
            try (FSDataOutputStream outputStream = fileSystem.append(path)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }catch (Exception e){
                System.out.println("Error 2");
            }
            
        }catch (Exception e){
            System.out.println("Error 3");
        }
    }
}

