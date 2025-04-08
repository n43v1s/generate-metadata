package org.example.generatemetadata;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class UsedMethodScanner {

    // Class you want to scan usage for
    private static final String targetClassSimpleName = "PreWarmGenericClient";
    private static final String targetClassFullName = "id.co.xl.lib.restclient.config.PreWarmGenericClient";

    public static void main(String[] args) throws IOException {
        Path projectDir = Paths.get("C:\\Users\\agusilaban\\xl\\billing-delivery");

        if (!Files.isDirectory(projectDir)) {
            System.err.println("Invalid project directory");
            return;
        }

        Set<String> usedMethods = new TreeSet<>();

        Pattern methodCallPattern = Pattern.compile(
                targetClassSimpleName + "\\s*\\.\\s*([a-zA-Z0-9_]+)\\s*\\(" // e.g., JsonProcessingException.someMethod(
        );

        Files.walk(projectDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(file -> {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        for (String line : lines) {
                            Matcher matcher = methodCallPattern.matcher(line);
                            while (matcher.find()) {
                                usedMethods.add(matcher.group(1));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        System.out.println("=== Used Methods for " + targetClassFullName + " ===");
        usedMethods.forEach(System.out::println);

        System.out.println("\n=== reflect-config.json Snippet ===");
        System.out.println("{");
        System.out.println("  \"name\": \"" + targetClassFullName + "\",");
        System.out.println("  \"methods\": [");
        Iterator<String> iterator = usedMethods.iterator();
        while (iterator.hasNext()) {
            String method = iterator.next();
            System.out.println("    { \"name\": \"" + method + "\" }" + (iterator.hasNext() ? "," : ""));
        }
        System.out.println("  ]");
        System.out.println("}");
    }
}

