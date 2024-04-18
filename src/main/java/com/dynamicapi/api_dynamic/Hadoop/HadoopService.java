package com.dynamicapi.api_dynamic.Hadoop;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Component;
import org.apache.hadoop.io.IOUtils;

import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class HadoopService {

    private FileSystem fileSystem;
    private static final int MAX_RETRIES = 3;  // Maximum number of retries
    private static final long RETRY_WAIT_TIME = 3000;  // Wait time between retries in milliseconds

    public HadoopService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void appendJsonStringToHdfsFile(String jsonString) throws IOException {
        Path path = new Path("hdfs://roseskull:8020/test/input/test.json");
        Path parentDir = path.getParent();

        if (!fileSystem.exists(parentDir)) {
            fileSystem.mkdirs(parentDir);
            System.out.println("Directory created: " + parentDir);
        }

        if (!fileSystem.exists(path)) {
            try (FSDataOutputStream output = fileSystem.create(path)) {
                output.writeChars(jsonString);
                System.out.println("File created");
                output.close();
            } catch (Exception e) {
                System.out.println("Error creating file: " + e.getMessage());
            }
        } else {
            appendWithRetry(path, jsonString);
        }
    }
    public void insertInputStream (InputStream input, String filename) throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Path path = new Path("hdfs://roseskull:8020/test/input/"+timeStamp+"_"+filename);
        Path parentDir = path.getParent();

        if (!fileSystem.exists(parentDir)) {
            fileSystem.mkdirs(parentDir);
            System.out.println("Directory created: " + parentDir);
        }

        if (!fileSystem.exists(path)) {
            FSDataOutputStream output = fileSystem.create(path);
            IOUtils.copyBytes(input, output, fileSystem.getConf());  
            System.out.println("File writen to HDFS"); 
        } else {
            System.out.println("File already exists");
        }

        
    }

    private void appendWithRetry(Path path, String jsonString) throws IOException {
        int retryCount = 0;
        boolean success = false;
        while (retryCount < MAX_RETRIES && !success) {
            try (FSDataOutputStream output = fileSystem.append(path)) {
                output.writeChars(jsonString);
                System.out.println("File appended");
                success = true; 
                output.close(); // Set success to true if append succeeds
            } catch (IOException e) {
                System.out.println("Attempt " + (retryCount + 1) + " failed: " + e.getMessage());
                if (retryCount < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_WAIT_TIME);  // Wait before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                }
                retryCount++;
            }
        }
        if (!success) {
            throw new IOException("Failed to append to file after " + MAX_RETRIES + " attempts");
        }
    }
}
