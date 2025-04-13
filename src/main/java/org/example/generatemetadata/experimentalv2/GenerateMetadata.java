package org.example.generatemetadata.experimentalv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.generatemetadata.experimental.ConsoleColors;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.generatemetadata.experimental.ConsoleColors.*;
import static org.example.generatemetadata.experimentalv2.ApplicationVariables.*;

public class GenerateMetadata {
    public static void main(String[] args) {
        initializeProject();

        scanProject();
        scanProjectImports();
        scanProjectPom();
        scanProjectDependencies();
        scanProjectFatJar();

        configureReflectConfig();
        configureProxyConfig();
        System.out.println("Finished Generating Metadata");
    }

    // ToDo : Project initialization (DONE)
    public static void initializeProject () {
        // System.out.println(BLUE + "[RUNNING] \t " + RESET + "Initializing project");
        try {
            printLogo();
            constructProjectGroupIdAndArtifactId();
            constructProjectMainDir();

            isAllPathsValid();
            listAllDependencies();
            buildFatJar();
        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project initialization: " + e.getMessage());
        }
        // System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project initialization complete\n");
    }

    // ToDo : Scan project for reflections and proxies ()
    public static void scanProject () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project scan finished\n");
    }

    // ToDo : Scan project's imports for reflections and proxies ()
    public static void scanProjectImports () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project imports scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project imports scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project imports scan finished\n");
    }

    // ToDo : Scan project's POM for reflections and proxies ()
    public static void scanProjectPom () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project POM scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project POM scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project pom POM finished\n");
    }

    // ToDo : Scan project's dependencies for reflections and proxies ()
    public static void scanProjectDependencies () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project dependencies scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project dependencies scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project dependencies scan finished\n");
    }

    // ToDo : Scan project's fat jar for reflections and proxies ()
    public static void scanProjectFatJar () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project fat jar scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project fat jar scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project fat jar scan finished\n");
    }

    // ToDo : Merge all output reflect configs ()
    public static void configureReflectConfig () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Configuring reflection metadata");
        try {

        } catch (Exception e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while configuring reflection metadata: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished configuring reflection metadata\n");
    }

    // ToDo : Merge all output proxy configs ()
    public static void configureProxyConfig () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Configuring proxy metadata");
        try {

        } catch (Exception e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while configuring proxy metadata: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished configuring proxy metadata\n");
    }

    // ToDo : Write reflect config (DONE)
    public static void writeReflectConfig (List<String> classList, Path path) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode reflection = objectMapper.createArrayNode();
        try {
            if (classList.isEmpty()) {
                System.out.println(YELLOW + "[WARN] \t " + RESET + "No classes found to write to file");
            } else {
                for (String className : classList) {
                    String fullClassName = className.replace("import", "").replace(";", "").trim();
                    ObjectNode classNode = objectMapper.createObjectNode();
                    classNode.put("name", fullClassName);
                    classNode.put("allDeclaredFields", true);
                    classNode.put("allDeclaredMethods", true);
                    classNode.put("allDeclaredConstructors", true);
                    reflection.add(classNode);
                }
                objectMapper.writeValue(new File(path.toString()), reflection);
                System.out.println(BLUE + "[INFO] \t " + RESET + "Wrote " + classList.size() + " classes to file: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "Unable to write to file: " + e.getMessage());
        }
    }

    // ToDo : Write proxy config (DONE)
    public static void writeProxyConfig (List<String> proxyList, Path path) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode proxyConfig = objectMapper.createArrayNode();
        try {
            if (proxyList.isEmpty()) {
                System.out.println(YELLOW + "[WARN] \t " + RESET + "No classes found to write to file");
            } else {
                for (String proxyName : proxyList) {
                    String fullClassName = proxyName.replace("import", "").replace(";", "").trim();
                    ObjectNode proxyNode = objectMapper.createObjectNode();
                    proxyNode.put("interfaces", fullClassName);
                    proxyConfig.add(proxyNode);
                }
                objectMapper.writeValue(new File(path.toString()), proxyConfig);
                System.out.println(BLUE + "[INFO] \t " + RESET + "Wrote " + proxyList.size() + " classes to file: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "Unable to write to file: " + e.getMessage());
        }
    }

    // ToDo : Path validation (DONE)
    public static void isAllPathsValid () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting path validation");
        try {
            Map<String, Path> pathMap = Map.of(
                    "projectPath", projectPath,
                    "repositoryPath", repositoryPath,
                    "outputPath", outputPath
            );
            Map<String, Path> reflectionFilePathMap = Map.of(
                    "projectReflectionPath", projectReflectionPath,
                    "importReflectionPath", importReflectionPath,
                    "pomReflectionPath", pomReflectionPath,
                    "dependenciesReflectionPath", dependenciesReflectionPath,
                    "fatJarReflectionPath", fatJarReflectionPath
            );

            Map<String, Path> proxyFilePathMap = Map.of(
                    "projectProxyPath", projectProxyPath,
                    "importProxyPath", importProxyPath,
                    "pomProxyPath", pomProxyPath,
                    "dependenciesProxyPath", dependenciesProxyPath,
                    "fatJarProxyPath", fatJarProxyPath
            );

            Map<String, Path> outputFilePathMap = Map.of(
                    "reflectConfigFile", reflectConfigFile,
                    "proxyConfigFile", proxyConfigFile
            );

            List<String> invalidPaths = new ArrayList<>();
            List<String> validPaths = new ArrayList<>();
            for (Map.Entry<String, Path> entry : pathMap.entrySet()){
                Path path = entry.getValue();
                if (!Files.exists(path) || !Files.isDirectory(path) || path.toString().isEmpty()){
                    invalidPaths.add(entry.getKey() + " -> " + path);
                } else {
                    validPaths.add(entry.getKey() + " -> " + path);
                }
            }

            List<String> invalidReflectionPaths = new ArrayList<>();
            List<String> validReflectionPaths = new ArrayList<>();
            for (Map.Entry<String, Path> entry : reflectionFilePathMap.entrySet()){
                File file = new File(entry.getValue().toString());
                Path path = entry.getValue();
                if (!file.exists()){
                    invalidReflectionPaths.add(entry.getKey() + " -> " + path);
                } else {
                    validReflectionPaths.add(entry.getKey() + " -> " + path);
                }
            }

            List<String> invalidProxyPaths = new ArrayList<>();
            List<String> validProxyPaths = new ArrayList<>();
            for (Map.Entry<String, Path> entry : proxyFilePathMap.entrySet()){
                File file = new File(entry.getValue().toString());
                Path path = entry.getValue();
                if (!file.exists()){
                    invalidProxyPaths.add(entry.getKey() + " -> " + path);
                } else {
                    validProxyPaths.add(entry.getKey() + " -> " + path);
                }
            }

            List<String> invalidOutputPaths = new ArrayList<>();
            List<String> validOutputPaths = new ArrayList<>();
            for (Map.Entry<String, Path> entry : outputFilePathMap.entrySet()){
                File file = new File(entry.getValue().toString());
                Path path = entry.getValue();
                if (!file.exists()){
                    invalidOutputPaths.add(entry.getKey() + " -> " + path);
                } else {
                    validOutputPaths.add(entry.getKey() + " -> " + path);
                }
            }

            if (invalidPaths.size() > 0 ||
                    invalidReflectionPaths.size() > 0 ||
                    invalidProxyPaths.size() > 0 ||
                    invalidOutputPaths.size() > 0
            ) {
                System.out.println(CYAN + "[INFO] \t\t "+ "Registered paths:" + RESET );
                for (String path : validPaths){System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + GREEN + "Valid path: " + RESET + path
                );}
                for (String path : invalidPaths){System.out.println(
                        RED + "[ERROR] \t " + RESET + RED + "Invalid path: " + RESET + path
                );}

                System.out.println(CYAN + "[INFO] \t\t "+ "Registered reflection paths:" + RESET );
                for (String path : validReflectionPaths){System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + GREEN + "Valid path: " + RESET + path
                );}
                for (String path : invalidReflectionPaths){System.out.println(
                        RED + "[ERROR] \t " + RESET + RED + "Invalid path: " + RESET + path
                );}

                System.out.println(CYAN + "[INFO] \t\t "+ "Registered proxy paths:" + RESET );
                for (String path : validProxyPaths){System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + GREEN + "Valid path: " + RESET + path
                );}
                for (String path : invalidProxyPaths){System.out.println(
                        RED + "[ERROR] \t " + RESET + RED + "Invalid path: " + RESET + path
                );}

                System.out.println(CYAN + "[INFO] \t\t " + "Registered output paths:" + RESET);
                for (String path : validOutputPaths){System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + GREEN + "Valid path: " + RESET + path
                );}
                for (String path : invalidOutputPaths){System.out.println(
                        RED + "[ERROR] \t " + RESET + RED + "Invalid path: " + RESET + path
                );}
                System.exit(0);
            } else {
                System.out.println(CYAN + "[INFO] \t\t " + RESET + "All paths are valid");
            }

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during path validation: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Path validation complete\n");
    }

    // ToDo : Run mvn dependency:run to list all project's dependencies (DONE)
    public static void listAllDependencies () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Listing all project dependencies");
        try {
            File workingDir = new File(projectPath.toString());
            System.out.println(CYAN + "[INFO] \t\t " + RESET + "Detected OS: " + os);
            File outputFile = new File(workingDir, mavenDependenciesListTxt);
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(mvnCommand, "dependency:list", ">", mavenDependenciesListTxt);

            processBuilder.directory(workingDir);
            Process process = processBuilder.start();
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );

            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println(RED + "[ERROR] \t " + RESET + errorLine);
                System.err.println(errorLine);
            }

            int exitCode = process.waitFor();
            System.out.println(
                    CYAN + "[INFO] \t\t " + RESET + "Maven command exited with code: " + exitCode
            );
            if (outputFile.exists()) {
                System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + "Dependencies written to: " + outputFile.getAbsolutePath()
                );
            } else {
                System.out.println(RED + "[ERROR] \t " + RESET + "Warning: dependency.txt was not created!");
            }

        } catch (IOException | InterruptedException e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while listing project dependencies: " + e.getMessage());
        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while listing project dependencies: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished listing all project dependencies\n");
    }

    // ToDo : Run mvn clean package -DskipTests to generate fat jar (DONE)
    public static void buildFatJar () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Building project fat jar");
        try {
            File workingDir = new File(projectPath.toString());
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(mvnCommand, "clean", "package", "-DskipTests");
            processBuilder.directory(workingDir);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + "Maven command exited with code: " + exitCode
                );
                System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + GREEN_BACKGROUND + "BUILD SUCCESS" + RESET
                );
            } else {
                System.out.println(RED + "[ERROR] \t " + RESET + RED_BACKGROUND + "BUILD FAILURE" + RESET);
                System.out.println(RED + "[ERROR] \t " + RESET + "Exit code: " + exitCode);
            }
            File fatjar = new File(projectPath.toString() + "\\target\\" + fatJarName);
            if (!fatjar.exists()) {
                System.out.println(YELLOW + "[WARN] \t\t " + RESET + "Application build success but cant find application's jar file");
            } else {
                System.out.println(
                        CYAN + "[INFO] \t\t " + RESET + "Fat jar found at: " + fatjar.getAbsolutePath()
                );
            }
        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while building project fat jar: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished Building project fat jar\n");
    }

    // ToDo : Just troll the users
    public static void printLogo() {
        try {
            ClassPathResource resource = new ClassPathResource("banner.txt");
            List<String> lines = Files.readAllLines(resource.getFile().toPath());
            lines.forEach(line -> {
                String line1 = substringSafe(line, 0, 32);
                String line2 = substringSafe(line, 34, 64);
                String line3 = substringSafe(line, 65, 90);
                System.out.println(
                        RED + line1 + RESET +
                        BLUE + line2 + RESET +
                        GREEN + line3 + RESET);
            });
            System.out.println("Metadata Generator Version : " + version + "\n");
        } catch (IOException e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "Unable to load logo: " + e.getMessage());
        }
    }
    private static String substringSafe(String str, int start, int end) {
        if (start >= str.length()) return "";
        return str.substring(start, Math.min(end, str.length()));
    }
}
