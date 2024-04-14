package com.dynamicapi.api_dynamic.Hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.hadoop.fs.FsShell;

import java.net.URI;

@org.springframework.context.annotation.Configuration
@ComponentScan("com.dynamicapi.api_dynamic.Hadoop")
public class HadoopConfig {

    private String hadoopFsUri = "hdfs://roseskull:8020"; 

    @Bean
    public Configuration hadoopConfiguration() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", hadoopFsUri);
        return configuration;
    }   

    @Bean
    public FileSystem fileSystem(Configuration configuration) throws Exception {
        return FileSystem.get(new URI(hadoopFsUri), configuration);
    }

    @Bean
    public FsShell fsShell(Configuration configuration) {
        return new FsShell(configuration);
    }
}

