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

public class ReflectConfigGenerator {

    public static void main(String[] args) throws IOException {
        String projectDir = "C:\\Users\\agusilaban\\xl\\esim-lifecycle-notification";
        String outputFile = "C:\\Users\\agusilaban\\xl\\metadatas\\reflect-config.json";
        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(Paths.get(projectDir))) {
            javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode reflectConfig = mapper.createArrayNode();
        for (Path javaFile : javaFiles) {
            processJavaFile(javaFile, reflectConfig);
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), reflectConfig);
        System.out.println("reflect-config.json generated at: " + outputFile);
    }

    private static void processJavaFile(Path javaFile, ArrayNode reflectConfig) throws IOException {
        List<String> lines = Files.readAllLines(javaFile);
        String packageName = null;
        String className = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("package ")) {
                packageName = line.split(" ")[1].replace(";", "");
            }
            if (line.startsWith("public class ") || line.startsWith("class ")) {
                line = line.replace("{", "");
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    className = parts[2];
                } else if (parts.length == 2) {
                    className = parts[1];
                }
            }
            if (line.startsWith("public static class ") || line.startsWith("static class ")) {
                line = line.replace("{", "");
                String[] parts = line.split(" ");
                if (parts.length >= 4 && packageName != null && className != null) {
                    String innerClassName = parts[3];
                    addClassToConfig(reflectConfig, removeGenerics(packageName + "." + className + "$" + innerClassName));
                }
            }
        }
        if (packageName != null && className != null) {
            addClassToConfig(reflectConfig, removeGenerics(packageName + "." + className));
        }
    }

    private static void addClassToConfig(ArrayNode reflectConfig, String fullClassName) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode classNode = mapper.createObjectNode();
        classNode.put("name", fullClassName);
        classNode.put("allDeclaredFields", true);
        classNode.put("allDeclaredMethods", true);
        classNode.put("allDeclaredConstructors", true);
        reflectConfig.add(classNode);
    }

    private static String removeGenerics(String className) {
        return className.replaceAll("<.*?>", "");
    }

}
