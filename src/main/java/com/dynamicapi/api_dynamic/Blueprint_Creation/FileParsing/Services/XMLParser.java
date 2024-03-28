package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;

@Component
public class XMLParser implements FileParsingInterface {

     @Override
    public String FileParser(InputStream file) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Set<String> tags = new HashSet<>();
        String keywords = "";
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);           
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            TagExtractor(document, tags);
            keywords = String.join(",", tags);


        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing file";
        }
        return keywords;
    }
    @Override
    public boolean supports(String type) {
        return type.equalsIgnoreCase("application/xml") || type.equalsIgnoreCase("text/xml");
    }

    private void TagExtractor(Node node , Set<String> tagNames){
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            tagNames.add(node.getNodeName());
        }

        // Recurse for each child
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            TagExtractor(children.item(i), tagNames);
        }
    }


     
}
