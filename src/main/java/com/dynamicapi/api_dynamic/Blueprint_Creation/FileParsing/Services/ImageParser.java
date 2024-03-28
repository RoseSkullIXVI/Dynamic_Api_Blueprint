package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class ImageParser implements FileParsingInterface{
    private static final Set<String> Supported_Image_Types = new HashSet<>();
        
    static {
        Supported_Image_Types.add("image/jpeg");
        Supported_Image_Types.add("image/png");
        Supported_Image_Types.add("image/gif");
        Supported_Image_Types.add("image/bmp");
        Supported_Image_Types.add("image/tiff");
        Supported_Image_Types.add("image/webp");
        Supported_Image_Types.add("image/heic");
        Supported_Image_Types.add("image/avif");
        Supported_Image_Types.add("image/x-icon");
        Supported_Image_Types.add("image/vnd.microsoft.icon");
    }

    @Override
    public String FileParser(InputStream file) {
        String keywords = "";
        List<String> image_tags = new ArrayList<>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
               image_tags.add(tag.toString());               
            }
            }
            Gson gson = new GsonBuilder().create();
            keywords = gson.toJson(image_tags);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keywords;
       
       
    }

    @Override
    public boolean supports(String type) {
        return Supported_Image_Types.contains(type);
    }
    
}
