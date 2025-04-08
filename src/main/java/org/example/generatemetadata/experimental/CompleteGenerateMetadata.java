package org.example.generatemetadata.experimental;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompleteGenerateMetadata {
    // Properties
    public static String projectName = "recharge-history";
    public static String excludedImportPrefix = "import id.co.xl."+ projectName +".";

    static final String RESET = "\033[0m";
    static final String CYAN = "\033[0;36m";
    static final String PURPLE = "\033[0;35m";

    // Input
    public static Path projectPath = Paths.get("C:\\Users\\agusilaban\\xl\\" + projectName);
    public static Path repositoryPath = Paths.get("C:\\Users\\agusilaban\\.m2\\repository\\");

    public static Path dependenciesListTxtPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\dependencies.txt");
    public static Path dependenciesListTxtPath2 = Paths.get("C:\\Users\\agusilaban\\xl\\"+projectName+"\\dependencies.txt");

    // Output
    public static Path serviceReflectionPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\experimental\\project-reflect-config.json");
    public static Path serviceProxyPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\experimental\\proxy-config.json");
    public static Path libraryReflectionPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\experimental\\library-reflect-config.json");
    public static Path importReflectionPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\experimental\\import-reflect-config.json");

    // Final Output
    public static Path reflectConfigPath = Paths.get("");
    public static Path proxyConfigPath = Paths.get("");

    public static void main(String[] args) throws IOException {
        // Todo: Ensure Paths (DONE)
        isValidPath();

        // Todo: List Service Reflections (DONE)
        listServiceReflection(projectPath);

        // Todo: List Service Proxies (DONE)
        listServiceProxy(projectPath);

        // Todo: List Imports Reflections (DONE)
        // Todo: List Imports Used Method Reflections (DONE)
        listImportReflection(projectPath);

        // Todo: List Libraries Reflections (DONE)
        // Todo: List Libraries Used Method Reflections
        // loadingScreen(); // in case you're bored
        System.out.println(PURPLE + "====   EXPERIMENTAL FOR DEPENDENCIES IN POM   ====" + RESET);
        listAllDependencies();

        // Todo: Generate Reflect Config JSON
        // Todo: Generate Proxy Config JSON
    }

    // Capture
    public static void listServiceReflection(Path projectPath) {
        System.out.println(CYAN + "\n=== STARTING REFLECTING PROJECT CLASSES ===" + RESET);
        Pattern packagePattern = Pattern.compile("^package\\s+([a-zA-Z0-9_.]+);");
        Pattern classPattern = Pattern.compile("^(public\\s+)?(static\\s+)?class\\s+(\\w+)");
        try {
            List<String> classList = new ArrayList<>();

            Files.walk(projectPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String packageName = "";
                            List<String> lines = Files.readAllLines(path);

                            for (String line : lines) {
                                line = line.trim();

                                Matcher packageMatcher = packagePattern.matcher(line);
                                if (packageMatcher.find()) {
                                    packageName = packageMatcher.group(1);
                                }

                                Matcher classMatcher = classPattern.matcher(line);
                                if (classMatcher.find()) {
                                    String className = classMatcher.group(3);
                                    String fullClass = packageName.isEmpty()
                                            ? className
                                            : packageName + "." + className;
                                    classList.add(fullClass);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            writeReflectConfig(classList, serviceReflectionPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n \t Project classes reflection written to " + serviceReflectionPath.toAbsolutePath());
        System.out.println(CYAN + "\n=== REFLECTING PROJECT CLASSES FINISHED ===\n" + RESET);
    }
    public static void listServiceProxy(Path projectPath){
        System.out.println(CYAN + "\n=== STARTING REFLECTING PROJECT INTERFACE ===" + RESET);
        Pattern packagePattern = Pattern.compile("^package\\s+([a-zA-Z0-9_.]+);");
        Pattern interfacePattern = Pattern.compile("^(public\\s+)?interface\\s+(\\w+)");
        try {
            List<String> proxyList = new ArrayList<>();
            Map<String, Set<String>> proxyMap = new HashMap<>();

            Files.walk(projectPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String packageName = "";
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                line = line.trim();

                                Matcher packageMatcher = packagePattern.matcher(line);
                                if (packageMatcher.find()) {
                                    packageName = packageMatcher.group(1);
                                }

                                Matcher intfMatcher = interfacePattern.matcher(line);
                                if (intfMatcher.find()) {
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
            writeProxyConfig(proxyList, serviceProxyPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n \t Project interface reflection written to " + serviceProxyPath.toAbsolutePath());
        System.out.println(CYAN + "\n=== REFLECTING PROJECT INTERFACE FINISHED ===\n" + RESET);
    }
    public static void listAllDependencies(){
        System.out.println(CYAN + "====   STARTING TO VERIFY DEPENDENCIES VERSION   ====" + RESET);
        try {
            Path pomPath = Paths.get(projectPath.toString()+ "\\pom.xml");
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(String.valueOf(pomPath)));
            List<Dependency> dependencies = model.getDependencies();
            List<String> unversionedLibraries = new ArrayList<>();
            List<String> versionedLibraries = new ArrayList<>();

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

            System.err.println("[UNVERSIONED DEPENDENCY]");
            for (String library : unversionedLibraries){
                System.err.println(library);
                String newLibrary = "";
                jarVersion = dependenciesExtractor(library);
                if (jarVersion == null){
                    System.err.println("There is matched dependency");
                    System.err.println(library + ":" + jarVersion + "\n");
                } else {
                    newLibrary = library + ":" + jarVersion;
                    versionedLibraries.add(newLibrary);
                    System.out.println("\nUnversioned dependency's version already discovered on list");
                    System.out.println("\t " + library + ":" + jarVersion + "\n");
                }
            }
            System.out.println(CYAN + "====   VERIFY DEPENDENCIES VERSION FINISHED   ====\n" + RESET);

            listDependenciesReflection(versionedLibraries);

//            for (String fixedLibrary : versionedLibraries){
//                System.out.println(fixedLibrary);
//                constructJarName(fixedLibrary);
//                System.out.println("\n");
//                listLibraryReflection(Paths.get(constructJarName(fixedLibrary)));
//                System.out.println("\n");
//            }

//            System.out.println("\nversioned libraries \n");
//            for (String fixedLibrary : versionedLibraries){
//                System.out.printf("%s:%s:%s %n",
//                        dep.getGroupId(),
//                        dep.getArtifactId(),
//                        dep.getVersion());
//                constructJarName(fixedLibrary);
//                System.out.println(fixedLibrary);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void listDependenciesReflection(List<String> libraryPaths){
        System.out.println(CYAN + "\n=== STARTING REFLECTING PROJECT'S DEPENDENCIES FROM POM ===" + RESET);
        try {
            List<String> validJars = new ArrayList<>();
            List<String> invalidJars = new ArrayList<>();
            String jarPath = "";
            String dependencyPath = "";
            for (String libraryPath : libraryPaths) {
                dependencyPath = constructJarName(libraryPath);
                jarPath = repositoryPath.toString().replace("\\", "/") + dependencyPath.toString().replace("\\", "/");
                System.out.println("Loading JAR from: " + jarPath);

                File jarFile = new File(jarPath);
                if (!jarFile.exists()) {
                    invalidJars.add(jarPath);
                    System.out.println("[ERR]No JAR file found at: " + jarPath + "\n");
                } else {
                    validJars.add(jarPath);
                    System.out.println("[OK] JAR file found at: " + jarPath + "\n");
                }
            }

            for (String validJar : validJars) {
                List<String> validClassList = new ArrayList<>();
                List<String> invalidClassList = new ArrayList<>();
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
                                try {
                                    Class<?> clazz = classLoader.loadClass(className);
                                    validClassList.add(clazz.getName());
                                    /**
                                    Arrays.stream(clazz.getDeclaredMethods())
                                            .forEach(method -> {
                                                String params = Arrays.stream(method.getParameters())
                                                        .map(p -> p.getType().getSimpleName() + " " + p.getName())
                                                        .collect(Collectors.joining(", "));
                                                System.out.println("  - " + method.getName() + " -> params: [" + params + "]");
                                            });
                                    **/

                                } catch (Throwable e) {
                                    invalidClassList.add(className);
                                }
                            });

                    // TODO : From this line should be a block of code for writing reflection into JSON format
                    System.out.println("=== Class List for "+validJar+" ===");

                    System.out.println("=== Valid Classes ===");
                    /**
                    for (String validClass : validClassList) {
                        System.out.println("Valid class: " + validClass);
                    }
                    **/
                    System.out.println("Total classes scanned: " + validClassList.size() + "\n");


                    System.out.println("=== Invalid Classes ===");
                    /**
                    for (String invalidClass : invalidClassList) {
                        System.out.println("Invalid class: " + invalidClass);
                    }
                    **/
                    System.out.println("Total classes failed to scanned: " + invalidClassList.size() + "\n");
                    // TODO : To this line should be a block of code for writing reflection into JSON format

                } catch (IOException e) {
                    System.err.println("Error reading JAR file: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(CYAN + "=== REFLECTING PROJECT'S DEPENDENCIES FROM POM FINISHED ===" + RESET);
    }
    public static void listImportReflection(Path projectPath){
        try {
            System.out.println(CYAN + "\n=== STARTING REFLECTING PROJECT'S IMPORTS ===" + RESET);
            System.out.println("\n \t Import Statements (excluding " + excludedImportPrefix + "*)");
            List<String> importList = new ArrayList<>();
            Map<String, Set<String>> importMap = new HashMap<>();

            Files.walk(projectPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                line = line.trim();
                                if (line.startsWith("import ") &&
                                        !line.startsWith(excludedImportPrefix) &&
                                        !line.endsWith(".*;") &&
                                        !line.contains("static ") &&
                                        !line.contains("import id.co.xl." + projectName + ".") &&
                                        !line.contains(".springframework.test.")
                                ) {
                                    importMap
                                            .computeIfAbsent(line, k -> new HashSet<>())
                                            .add(projectPath.relativize(path).toString());
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + path);
                        }
                    });

            importMap.keySet().stream()
                    .sorted()
                    .forEach(importedClass -> {
                        // System.out.println(importedClass);
                        importMap.get(importedClass);
                        importList.add(importedClass);
                    });

            writeReflectConfig(importList, importReflectionPath);
            System.out.println(" \t Project's imports reflection written to " + importReflectionPath.toAbsolutePath());
            System.out.println(CYAN + "\n=== REFLECTING PROJECT's IMPORTS FINISHED ===\n" + RESET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utils
    public static void loadingScreen(){
        String[] frames = {"|", "/", "-", "\\"};
        for (int i = 0; i < 10; i++) {
            System.out.print(PURPLE + "\rProcessing " + frames[i % 4] + " " + RESET);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println(PURPLE + "\rDone!                  " + RESET);
    }
    public static void isValidPath(){
        try {
            Map<String, Path> pathMap = Map.of(
                    "projectPath", projectPath,
                    "repositoryPath", repositoryPath
            );
            Map<String, Path> filePathMap = Map.of(
                    "serviceReflectionPath", serviceReflectionPath,
                    "serviceProxyPath", serviceProxyPath,
                    "libraryReflectionPath", libraryReflectionPath,
                    "importReflectionPath", importReflectionPath
            );

            List<String> invalidPaths = new ArrayList<>();
            List<String> validPaths = new ArrayList<>();

            for (Map.Entry<String, Path> entry : pathMap.entrySet()){
                Path path = entry.getValue();
                if (!Files.exists(path) || !Files.isDirectory(path) || path.toString().isEmpty()){
                    invalidPaths.add(entry.getKey() + " -> " + path.toString());
                } else {
                    validPaths.add(entry.getKey() + " -> " + path.toString());
                }
            }

            for (Map.Entry<String, Path> entry : filePathMap.entrySet()){
                File file = new File(entry.getValue().toString());
                Path path = entry.getValue();
                if (!file.exists()){
                    invalidPaths.add(entry.getKey() + " -> " + path.toString());
                } else {
                    validPaths.add(entry.getKey() + " -> " + path.toString());
                }
            }

            for (String path : invalidPaths){
                System.err.println("Invalid path: " + path);
            }
            for (String path : validPaths){
                System.out.println("Valid path: " + path);
            }

            if (invalidPaths.size() > 0){
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
    public static void writeReflectConfig(List<String> classList, Path reflectConfigPath) {
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
    public static void writeProxyConfig(List<String> proxyList, Path proxyConfigPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode proxyConfig = mapper.createArrayNode();
            for (String proxyName : proxyList){
                String fullClassName = proxyName.replace("import ", "").replace(";", "").trim();
                ObjectNode classNode = mapper.createObjectNode();
                classNode.put("interface", fullClassName);
                proxyConfig.add(classNode);
            }
            mapper.writeValue(new File(proxyConfigPath.toString()), proxyConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String removeGenerics(String className) {
        return className.replaceAll("<.*?>", "");
    }

    // Tools For Dependencies/Jar/Library
    public static String dependenciesExtractor(String jarName){
        Map<String, String> dependenciesMap = new HashMap<>();
        String dependencyVersion = "";
        try {
            File inputFile = new File(dependenciesListTxtPath.toUri());
            if (!inputFile.exists()) {
                System.err.println("Dependency list file not found: " + inputFile.getAbsolutePath());
                return "file not found";
            }
            Pattern pattern = Pattern.compile("^\\[INFO\\]\\s+(.*?):(.*?):jar:(.*?):(compile|runtime|test).*");

            try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().startsWith("[INFO]") && line.contains(":jar:")) {
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
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                e.printStackTrace();
            }

            dependencyVersion = dependenciesMap.entrySet().stream()
                    .filter(entry -> entry.getKey().contains(jarName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dependencyVersion;
    }
    public static String constructJarName(String dependency){
        String jarName = "";
        try {
            String[] parts = dependency.split(":");
            if (parts.length != 3) {
                System.err.println("\t Invalid format. \n \t Expected Format: groupId:artifactId:version");
                return jarName;
            }
            String groupId = parts[0].replace('.', '\\');
            String artifactId = parts[1];
            String version = parts[2];
            jarName = "\\" + groupId + "\\" + artifactId + "\\" + version + "\\" + artifactId + "-" + version + ".jar";

            System.out.println("\nDependency: " + dependency);
            // System.out.println("Converted path: " + jarName + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jarName;
    }

    // Todo: After completing reflecting dependencies
    private static void listMemoryDumpReflection () {}
}

