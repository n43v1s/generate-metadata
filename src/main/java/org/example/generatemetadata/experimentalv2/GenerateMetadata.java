package org.example.generatemetadata.experimentalv2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.example.generatemetadata.experimental.ConsoleColors;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    // ToDo : Scan project for reflections and proxies (DONE)
    public static void scanProject () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project scan");
        try {
            String basePackage = excludedImportPrefix;
            Pattern packagePattern = Pattern.compile("^package\\s+([a-zA-Z0-9_.]+);");
            Pattern classPattern = Pattern.compile("^(public\\s+)?(static\\s+)?class\\s+(\\w+)");
            Pattern interfacePattern = Pattern.compile("^(public\\s+)?interface\\s+(\\w+)");
            List<String> classList = new ArrayList<>();
            List<String> proxyList = new ArrayList<>();

            Files.walk(projectPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String packageName = "";
                            boolean isInTargetPackage = false;
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                line = line.trim();
                                Matcher packageMatcher = packagePattern.matcher(line);
                                if (packageMatcher.find()) {
                                    packageName = packageMatcher.group(1);
                                }
                                isInTargetPackage = packageName.startsWith(basePackage.replace("import ", ""));

                                Matcher classMatcher = classPattern.matcher(line);
                                if (classMatcher.find() && isInTargetPackage) {
                                    String className = classMatcher.group(3);
                                    String fullClass = packageName.isEmpty()
                                            ? className
                                            : packageName + "." + className;
                                    classList.add(fullClass);
                                }

                                Matcher intfMatcher = interfacePattern.matcher(line);
                                if (intfMatcher.find() && isInTargetPackage) {
                                    String interfaceName = intfMatcher.group(2);
                                    String fullInterface = packageName.isEmpty()
                                            ? interfaceName
                                            : packageName + "." + interfaceName;
                                    proxyList.add(fullInterface);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            writeReflectConfig(classList, projectReflectionPath);
            writeProxyConfig(proxyList, projectProxyPath);
        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project scan finished\n");
    }

    // ToDo : Scan project's imports for reflections and proxies (1/2 need to filter for interfaces)
    public static void scanProjectImports () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project imports scan");
        try {
            List<String> classList = new ArrayList<>();
            List<String> interfaceList = new ArrayList<>();
            String excludedPrefix = excludedImportPrefix;
            Map<String, Set<String>> importClassMap = new HashMap<>();
            Map<String, Set<String>> importInterfaceMap = new HashMap<>();
            Map<String, String> dependencyList = getDependenciesRepository(getDependenciesList());

            Files.walk(projectPath)
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
                                        !line.contains(".springframework.test.")
                                ) {
                                    importClassMap
                                            .computeIfAbsent(line, k -> new HashSet<>())
                                            .add(projectPath.relativize(path).toString());
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + path);
                        }
                    });

            importClassMap.keySet().stream()
                    .sorted()
                    .forEach(imp -> {
//                        System.out.println(imp);
                        importClassMap.get(imp);
                        classList.add(imp);
                    });

//            dependencyList.forEach((key, value) -> {
//                System.out.println("key -> " + key);
//                System.out.println("value -> " + value);
//            });

            for (String importedClass : classList){
                List<String> matchedDependency = new ArrayList<>();
                importedClass.replace("import ", "");
                String[] importedClassParts = importedClass.split(".");

                dependencyList.forEach((key, value) -> {
                    key.contains(importedClass);
                });
            }

            writeReflectConfig(classList, importReflectionPath);
        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project imports scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project imports scan finished\n");
    }

    // ToDo : Scan project's POM for reflections and proxies (DONE)
    public static void scanProjectPom () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project POM scan");
        try {
            Path pomPath = Paths.get(projectPomPath.toUri());
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(String.valueOf(pomPath)));
            List<Dependency> dependencies = model.getDependencies();
            List<String> unversionedLibraries = new ArrayList<>();
            List<String> versionedLibraries = new ArrayList<>();
            List<String> validJars = new ArrayList<>();
            List<String> invalidJars = new ArrayList<>();
            List<String> validClassList = new ArrayList<>();
            List<String> invalidClassList = new ArrayList<>();
            List<String> interfaceList = new ArrayList<>();
            Map<String, String> dependencyList = getDependenciesList();
            Map<String, Map<String, String>> completeDependenciesMap = new HashMap<>();
            String jarPath = "";
            String dependencyPath = "";

            String fullJarPath = "";
            String libraryName = "";
            String jarVersion = "";
            for (Dependency dep : dependencies) {
                if (dep.getGroupId().startsWith("org.springframework") || dep.getGroupId().startsWith("org.springdoc")) {
                    continue;
                }
                if (dep.getVersion() == null || dep.getVersion().startsWith("${")) {
                    libraryName = dep.getGroupId().toString() + ":" + dep.getArtifactId().toString();
                    unversionedLibraries.add(libraryName);
                } else {
                    fullJarPath = dep.getGroupId().toString() + ":" + dep.getArtifactId().toString() + ":" + dep.getVersion();
                    versionedLibraries.add(fullJarPath);
                }
            }

            System.out.println(YELLOW + "[WARN]\t\t " + "[UNVERSIONED DEPENDENCY]" + RESET );
            for (String library : unversionedLibraries){
                System.out.println(CYAN + "[INFO]\t\t " + RESET + RED + library + RESET);
                String newLibrary = "";
                jarVersion = dependencyList.get(library);
                if (jarVersion == null){
                    System.out.println(RED + "\t\t\t There is matched dependency" + RESET);
                    System.out.println(RED + "\t\t\t " + library + ":" + jarVersion + RESET);
                } else {
                    newLibrary = library + ":" + jarVersion;
                    versionedLibraries.add(newLibrary);
                    System.out.println(CYAN + "[INFO]\t\t " + RESET + "Unversioned dependency's version already discovered on list");
                    System.out.println(CYAN + "[INFO]\t\t " + RESET + GREEN + library + ":" + jarVersion + RESET);
                }
            }

            for (String libraryPath : versionedLibraries) {
                String[] parts = libraryPath.split(":");
                if (parts.length != 3) {
                    System.out.println("\t Invalid format. \n \t Expected Format: groupId:artifactId:version");
                }
                String groupId = parts[0].replace('.', '\\');
                String artifactId = parts[1];
                String version = parts[2];
                dependencyPath = "\\" + groupId + "\\" + artifactId + "\\" + version + "\\" + artifactId + "-" + version + ".jar";

                jarPath = repositoryPath.toString().replace("\\", "/") + dependencyPath.toString().replace("\\", "/");

                File jarFile = new File(jarPath);
                if (!jarFile.exists()) {
                    invalidJars.add(jarPath);
                    System.out.println(RED + "[ERROR]\t\t" + RESET + RED + "[ERR]" + RESET + "No JAR file found at: " + jarPath);
                } else {
                    validJars.add(jarPath);
                    System.out.println(CYAN + "[INFO]\t\t " + RESET + GREEN + "[OK]" + RESET + "JAR file found at: " + jarPath);
                }
            }

            for (String validJar : validJars) {
                File jarFile = new File(validJar);
                try {
                    JarFile jar = new JarFile(jarFile);
                    Enumeration<JarEntry> entriesEnum = jar.entries();
                    URLClassLoader classLoader = new URLClassLoader(
                            new URL[]{jarFile.toURI().toURL()},
                            Thread.currentThread().getContextClassLoader());

                    Collections.list(entriesEnum).stream()
                            .filter(entry -> entry.getName().endsWith(".class"))
                            .forEach(entry -> {
                                String className = entry.getName().replace("/", ".").replace(".class", "");
                                Map<String, String> methodList = new HashMap<>();
                                try {
                                    Class<?> clazz = classLoader.loadClass(className);
                                    if (clazz.isInterface()) {
                                        interfaceList.add(clazz.getName());
                                    } else {
                                        validClassList.add(clazz.getName());
                                        Arrays.stream(clazz.getDeclaredMethods())
                                                .forEach(method -> {
                                                    String params = Arrays.stream(method.getParameters())
                                                            .map(p -> p.getType().getClass().getName()).collect(Collectors.toList()).toString();
                                                    methodList.put(method.getName(), params);
                                                });
                                        completeDependenciesMap.put(className, methodList);
                                    }
                                } catch (Throwable e) {
                                    invalidClassList.add(className);
                                }
                            });
                    count += completeDependenciesMap.size();
                }
                catch (IOException e) {
                    System.err.println("Error reading JAR file: " + e.getMessage());
                }
            }

            writeReflectConfig(validClassList, pomReflectionPath);
            writeProxyConfig(interfaceList, pomProxyPath);
        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project POM scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project pom POM finished\n");
    }

    // ToDo : Scan project's dependencies for reflections and proxies (DONE)
    public static void scanProjectDependencies () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project dependencies scan");
        try {
            Map<String, String> dependencyList = getDependenciesRepository(getDependenciesList());
            List<String> validJars = new ArrayList<>();
            List<String> invalidJars = new ArrayList<>();

            String dependencyPath = "";

            Map<String, Map<String, String>> completeDependenciesMap = new HashMap<>();
            List<String> validClassList = new ArrayList<>();
            List<String> invalidClassList = new ArrayList<>();
            List<String> validInterfaceList = new ArrayList<>();
            List<String> invalidInterfaceList = new ArrayList<>();

            dependencyList.forEach((key, value) -> {
                String jarPath = String.valueOf(repositoryPath + value);
//                System.out.println("Loading JAR from: " + jarPath);

                File jarFile = new File(jarPath);
                if (!jarFile.exists()) {
                    invalidJars.add(jarPath);
//                    System.out.println(RED + "[ERR]" + RESET + "No JAR file found at: " + jarPath);
                } else {
                    validJars.add(jarPath);
//                    System.out.println(GREEN + "[OK]" + RESET + " JAR file found at: " + jarPath + "\n");
                }
            });

            for (String validJar : validJars) {
                File jarFile = new File(validJar);
                JarFile jar = new JarFile(jarFile);
                Enumeration<JarEntry> entriesEnum = jar.entries();
                URLClassLoader classLoader = new URLClassLoader(
                        new URL[]{jarFile.toURI().toURL()},
                        Thread.currentThread().getContextClassLoader());

                Collections.list(entriesEnum).stream()
                        .filter(entry -> entry.getName().endsWith(".class"))
                        .forEach(entry -> {
                            String className = entry.getName().replace("/", ".").replace(".class", "");
                            Map<String, String> methodList = new HashMap<>();
                            try {
                                Class<?> clazz = classLoader.loadClass(className);
                                if (clazz.isInterface()) {
                                    validInterfaceList.add(clazz.getName());
                                } else {
                                    validClassList.add(clazz.getName());
                                    Arrays.stream(clazz.getDeclaredMethods())
                                            .forEach(method -> {
                                                String params = Arrays.stream(method.getParameters())
                                                        .map(p -> p.getType().getClass().getName()).collect(Collectors.toList()).toString();
                                                methodList.put(method.getName(), params);
                                            });
                                    completeDependenciesMap.put(className, methodList);
                                }
                            } catch (Throwable e) {
                                invalidClassList.add(className);
                            }
                        });
                }
            writeReflectConfig(validClassList, dependenciesReflectionPath);
            writeProxyConfig(validInterfaceList, dependenciesProxyPath);

            System.out.println(CYAN + "[INFO] \t\t " + RESET + PURPLE + "Libraries found: " + RESET + validJars.size());
            System.out.println(CYAN + "[INFO] \t\t " + RESET + PURPLE + "Classes found: " + RESET + validClassList.size());
            System.out.println(CYAN + "[INFO] \t\t " + RESET + PURPLE + "Interfaces found: " + RESET + validInterfaceList.size());
        } catch (IOException ioe) {
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while reading library's JAR: " + ioe.getMessage());
        } catch (Exception e) {
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

    // ToDo : Merge all output reflect configs (DONE)
    public static void configureReflectConfig () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Configuring reflection metadata");
        try {
            int totalEntriesProcessed = 0;
            int duplicatedEntries = 0;
            ObjectMapper objectMapper = new ObjectMapper();
            String[] files = {
                    String.valueOf(projectReflectionPath),
                    String.valueOf(dependenciesReflectionPath),
                    String.valueOf(importReflectionPath),
                    String.valueOf(pomReflectionPath),
            };
            Map<String, JsonNode> metadata = new HashMap<>();
            for (String filePath : files) {
                File file = new File(filePath);
                if (!file.exists()) { System.out.println("File does not exist: " + filePath); continue; }
                JsonNode jsonNode = objectMapper.readTree(file);
                if (!jsonNode.isArray()){ System.out.println("File is not an array: " + filePath); continue; }
                for (JsonNode node : jsonNode) {
                    JsonNode classNameNode = node.get("name");
                    if (classNameNode != null && !classNameNode.isNull()) {
                        String name = classNameNode.asText();
                        if (!metadata.containsKey(name)) {
                            metadata.put(name, node);
                        } else {
//                            System.out.println("  Duplicate entry skipped: " + name);
                            duplicatedEntries += 1;
                        }
                    } else {
                        System.err.println("  Entry missing 'name' field: " + node);
                    }
                }
                totalEntriesProcessed += metadata.size();

                if (metadata.size() == 0) { System.err.println("Warning: No valid entries found in any input files!"); }
            }
            ArrayNode arrayNode = objectMapper.createArrayNode();
            metadata.values().stream()
                    .sorted(Comparator.comparing(key -> key.get("name").asText()))
                    .forEach(arrayNode::add);
            System.out.println(CYAN + "[INFO] \t\t " + RESET + "Total unique entries found: " + metadata.size());
            System.out.println(CYAN + "[INFO] \t\t " + RESET + "Total duplicated entries: " + duplicatedEntries);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(String.valueOf(reflectConfigFile)), arrayNode);
        } catch (IOException e) {
            System.out.println("Error reading file: ");
            e.printStackTrace();
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
                System.out.println(YELLOW + "[WARN] \t\t " + RESET + "No classes found to write to file");
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
                System.out.println(CYAN + "[INFO] \t\t " + RESET + "Wrote " + classList.size() + " classes to file: " + path.toAbsolutePath());
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
                System.out.println(YELLOW + "[WARN] \t\t " + RESET + "No interfaces found to write to file");
            } else {
                for (String proxyName : proxyList) {
                    String fullClassName = proxyName.replace("import", "").replace(";", "").trim();
                    ObjectNode proxyNode = objectMapper.createObjectNode();
                    ArrayNode arrayNode = objectMapper.createArrayNode();
                    arrayNode.add(fullClassName);
                    proxyNode.put("interfaces", arrayNode);
                    proxyConfig.add(proxyNode);
                }
                objectMapper.writeValue(new File(path.toString()), proxyConfig);
                System.out.println(CYAN + "[INFO] \t\t " + RESET + "Wrote " + proxyList.size() + " interfaces to file: " + path.toAbsolutePath());
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

    public static Map<String, String> getDependenciesList () {
        try {
            Map<String, String> dependenciesMap = new HashMap<>();
            File inputFile = new File(String.valueOf(projectPath.resolve(mavenDependenciesListTxt)));
            if (!inputFile.exists()) {
                System.out.println(YELLOW + "[WARN] \t " + RESET + "dependencies.txt not found");
                return null;
            }
            Pattern pattern = Pattern.compile("^\\[INFO\\]\\s+(.*?):(.*?):jar:(.*?):(compile|runtime|test).*");
            BufferedReader bufferedReader = new BufferedReader(new FileReader((inputFile)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().startsWith("[INFO]") && line.contains(":jar") ) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        String groupId = matcher.group(1);
                        String artifactId = matcher.group(2);
                        String version = matcher.group(3);
                        String fullName = groupId + ":" + artifactId;
                        dependenciesMap.put(fullName, version);
                    }
                }
            }
            dependenciesMap.entrySet().removeIf(entry ->
                    entry.getKey().toLowerCase().contains("spring") ||
                            entry.getValue().toLowerCase().contains("spring")
            );

            return dependenciesMap;
        } catch (IOException e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while reading the dependencies.txt file: " + e.getMessage());
        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while reading the dependencies.txt file: " + e.getMessage());
        }
        return null;
    }

    public static Map<String, String> getDependenciesRepository (Map<String, String> dependencyList) {
        try {
            Map<String, String> dependencyRepositoryList = new HashMap<>();
            dependencyList.forEach((key, value) -> {
                String[] parts = key.split(":");
                String groupId = parts[0].replace(".", directorySlash);
                String artifactId = parts[1];
                String version = value;
                String jarName = directorySlash +
                        groupId + directorySlash +
                        artifactId + directorySlash +
                        version + directorySlash +
                        artifactId + "-" + version + ".jar";
                dependencyRepositoryList.put(key, jarName);
//                System.out.println(CYAN + "[INFO] \t\t " + RESET + "Dependency: " + key + " -> " + jarName);

            });
            return dependencyRepositoryList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
