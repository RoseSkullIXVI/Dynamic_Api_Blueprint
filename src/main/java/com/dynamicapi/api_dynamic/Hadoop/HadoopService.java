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

}
