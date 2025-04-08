package org.example.generatemetadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.ArrayList;

public class ListImports {
    public static void main(String[] args) throws IOException {
        Path projectDir = Paths.get("C:\\Users\\agusilaban\\xl\\recharge-history");
        Path reflectConfigPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\reflect-config-1.json");
        String serviceName = "recharge-history";

        listImports(projectDir, serviceName, reflectConfigPath);
    }

    public static void listImports(Path servicePath, String serviceName, Path configPath) throws IOException {
        try {
            if (!Files.exists(servicePath) || !Files.isDirectory(servicePath)) {
                System.err.println("Invalid project directory.");
                return;
            }

            List<String> classList = new ArrayList<>();

            // Exclude this prefix
            String excludedPrefix = "import id.co.xl."+ serviceName +".";

            // Store import lines and their source files
            Map<String, Set<String>> importMap = new HashMap<>();

            // Walk the file tree and process .java files
            Files.walk(servicePath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                line = line.trim();
                                if (line.startsWith("import ") &&
                                        !line.startsWith(excludedPrefix) &&
                                        !line.endsWith(".*;") &&
                                        !line.contains("static ") &&
                                        !line.contains("import id.co.xl." + serviceName + ".") &&
                                        !line.contains(".springframework.test.")
                                ) {
                                    importMap
                                            .computeIfAbsent(line, k -> new HashSet<>())
                                            .add(servicePath.relativize(path).toString());
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + path);
                        }
                    });

            // Print all unique imports and their source files
            importMap.keySet().stream()
                    .sorted()
                    .forEach(imp -> {
                        System.out.println(imp);
                        importMap.get(imp);
                        classList.add(imp);
                    });

            writeImportReflection(classList, configPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeImportReflection(List<String> classList, Path reflectConfigPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode reflectConfig = mapper.createArrayNode();
            for (String className : classList){
                String fullClassName = className.replace("import ", "").replace(";", "").trim();
                ObjectNode classNode = mapper.createObjectNode();
                classNode.put("name", fullClassName);
                classNode.put("allDeclaredFields", true);
                classNode.put("allDeclaredMethods", true);
                classNode.put("allDeclaredConstructors", true);
                reflectConfig.add(classNode);
            }

            mapper.writeValue(new File(reflectConfigPath.toString()), reflectConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
