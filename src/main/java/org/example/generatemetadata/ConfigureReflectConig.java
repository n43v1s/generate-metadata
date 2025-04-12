package org.example.generatemetadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ConfigureReflectConig {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        String[] files = {
                "C:\\Users\\agusilaban\\Downloads\\agus\\experimental\\dependency-reflect-config.json",
                "C:\\Users\\agusilaban\\Downloads\\agus\\experimental\\import-reflect-config.json",
                "C:\\Users\\agusilaban\\Downloads\\agus\\experimental\\project-reflect-config.json",
                "C:\\Users\\agusilaban\\Downloads\\agus\\fatJatExperimental\\reflect-config.json"
        };

        String outputFile = "C:\\Users\\agusilaban\\Downloads\\agus\\saved-reflect-config.json";

        try {
            generateMetadata(files, outputFile);
            System.out.println("Successfully merged files to " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateMetadata(String[] filePaths, String outputFile) {
        Map<String, JsonNode> metadata = new HashMap<>();
        int totalEntriesProcessed = 0;
        try {
           for (String filePath : filePaths) {
               File file = new File(filePath);
               if (!file.exists()) { System.out.println("File does not exist: " + filePath); continue; }
               try {
                   JsonNode jsonNode = objectMapper.readTree(file);
                   if (!jsonNode.isArray()){ System.out.println("File is not an array: " + filePath); continue; }
                   for (JsonNode node : jsonNode) {
                       JsonNode classNameNode = node.get("name");
                       if (classNameNode != null && !classNameNode.isNull()) {
                           String name = classNameNode.asText();
                           if (!metadata.containsKey(name)) {
                               metadata.put(name, node);
                           } else {
                               System.out.println("  Duplicate entry skipped: " + name);
                           }
                       } else {
                           System.err.println("  Entry missing 'name' field: " + node);
                       }
                   }
               } catch (IOException e) {
                   System.out.println("Error reading file: " + filePath);
                   e.printStackTrace();
               }
               System.out.println("\nTotal unique entries found: " + metadata.size());
               System.out.println("Total entries processed: " + totalEntriesProcessed);

               if (metadata.size() == 0) { System.err.println("Warning: No valid entries found in any input files!"); }
           }

           ArrayNode arrayNode = objectMapper.createArrayNode();
           metadata.values().stream()
                   .sorted(Comparator.comparing(key -> key.get("name").asText()))
                   .forEach(arrayNode::add);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), arrayNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
