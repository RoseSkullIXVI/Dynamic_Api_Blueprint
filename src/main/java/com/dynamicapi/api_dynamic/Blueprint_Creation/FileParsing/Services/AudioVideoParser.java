package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.drew.metadata.Tag;
import com.drew.metadata.Metadata;

@Component
public class AudioVideoParser implements FileParsingInterface {
    private static final Set<String> Supported_Video_Audio_Types = new HashSet<>();
    static {
        Supported_Video_Audio_Types.add("audio/wav");
        Supported_Video_Audio_Types.add("video/mp4");
        Supported_Video_Audio_Types.add("video/quicktime");
        Supported_Video_Audio_Types.add("video/x-msvideo");
    }

    @Override
    public String FileParser(InputStream file) {
        String keywords = "";
        List<String> audio_video_tags = new ArrayList<>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                audio_video_tags.add(tag.toString());               
            }
            }
            Gson gson = new GsonBuilder().create();
            keywords = gson.toJson(audio_video_tags);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keywords;
    }

    @Override
    public boolean supports(String type) {
        return Supported_Video_Audio_Types.contains(type);
    }


    
}
