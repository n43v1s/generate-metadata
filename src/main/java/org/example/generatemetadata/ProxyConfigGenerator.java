package org.example.generatemetadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProxyConfigGenerator {
    public static void main(String[] args) throws IOException {
        String projectDir = "C:\\Users\\agusilaban\\xl\\esim-lifecycle-notification";
        String outputFile = "C:\\Users\\agusilaban\\xl\\metadatas\\proxy-config.json";

        List<Path> javaFiles;

        // Scan all .java files in the project directory
        try (Stream<Path> paths = Files.walk(Paths.get(projectDir))) {
            javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }

        // Create JSON structure
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode reflectConfig = mapper.createArrayNode();

        for (Path javaFile : javaFiles) {
            processJavaFile(javaFile, reflectConfig);
        }

        // Write JSON to file
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), reflectConfig);

        System.out.println("proxy-config.json generated at: " + outputFile);
    }

    private static void processJavaFile(Path javaFile, ArrayNode reflectConfig) throws IOException {
        List<String> lines = Files.readAllLines(javaFile);

        String packageName = null;
        String className = null;

        for (String line : lines) {
            line = line.trim();

            // Capture package name
            if (line.startsWith("package ")) {
                packageName = line.split(" ")[1].replace(";", "");
            }

            // Capture class name
            if (line.startsWith("public interface ") || line.startsWith("interface ")) {
                line = line.replace("{", "");
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    className = parts[2];
                } else if (parts.length == 2) {
                    className = parts[1];
                }
            }

            // Check for inner classes
            if (line.startsWith("public static interface ") || line.startsWith("static interface ")) {
                line = line.replace("{", "");
                String[] parts = line.split(" ");
                if (parts.length >= 4 && packageName != null && className != null) {
                    String innerClassName = parts[3];
                    addClassToConfig(reflectConfig, removeGenerics(packageName + "." + className + "$" + innerClassName));
                }
            }
        }

        // Add the main class to the config if applicable
        if (packageName != null && className != null) {
            addClassToConfig(reflectConfig, removeGenerics(packageName + "." + className));
        }
    }


    private static void addClassToConfig(ArrayNode reflectConfig, String fullClassName) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add(fullClassName);
        ObjectNode classNode = mapper.createObjectNode();
        classNode.put("interfaces", arrayNode);
        reflectConfig.add(classNode);
    }

    private static String removeGenerics(String className) {
        return className.replaceAll("<.*?>", "");
    }

}
