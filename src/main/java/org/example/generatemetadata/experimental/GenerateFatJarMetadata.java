package org.example.generatemetadata.experimental;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.sisu.space.asm.ClassReader;
import org.eclipse.sisu.space.asm.Opcodes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.generatemetadata.experimental.ConsoleColors.*;

public class GenerateFatJarMetadata {
    public static int count = 0;
    public static String projectName = "device-bundling-esim";
    public static boolean includeSpring = false;
    public static Path projectPath = Paths.get("C:\\Users\\agusilaban\\xl\\" + projectName);
    public static Path parentOutputPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\fatJatExperimental");
    public static Path reflectConfigFile = Paths.get("reflect-config.json");
    public static Path proxyConfigFile = Paths.get("proxy-config.json");

    public static void main(String[] args) {
        System.out.println("Hello world!");
        pathValidation();

        buildFatJar();

        listLibrary(constructJarFilePath(constructJarFileName()));
    }


    // List Library
    public static Map<String, Map<String, List<String>>> listLibrary(String jarPath) {
        Map<String, Map<String, List<String>>> libraryContents = new LinkedHashMap<>();
        List<String> classes = new ArrayList<>();
        List<String> interfaces = new ArrayList<>();
        try (FileSystem fs = FileSystems.newFileSystem(Paths.get(jarPath), (ClassLoader) null)) {
            List<Path> libraries = Files.list(fs.getPath("BOOT-INF/lib"))
                    .filter(p -> p.toString().endsWith(".jar"))
                    .filter(q -> includeSpring ? !q.toString().contains("springIncluded") : !q.toString().contains("spring"))
                    .collect(Collectors.toList());
            for (Path library : libraries) {
                String libName = library.getFileName().toString();
                List<String> libraryClasses = new ArrayList<String>();
                List<String> libraryInterfaces = new ArrayList<String>();
                try (FileSystem libFs = FileSystems.newFileSystem(library, (ClassLoader) null)) {
                    Files.walk(libFs.getPath("/"))
                            .filter(p -> p.toString().endsWith(".class"))
                            .filter(q -> !q.toString().contains("META-INF"))
                            .forEach(classFile -> {
                                try (InputStream is = Files.newInputStream(classFile)) {
                                    ClassReader reader = new ClassReader(is);
                                    String className = classFile.toString()
                                            .replace("/", ".")
                                            .replace(".class", "")
                                            .substring(1);
                                    if ((reader.getAccess() & Opcodes.ACC_INTERFACE) != 0) {
                                        interfaces.add(className);
                                        libraryInterfaces.add(className);
                                    } else {
                                        classes.add(className);
                                        libraryClasses.add(className);
                                    }
                                } catch (IOException e) {
                                    System.err.println(RED + "Error reading: " + classFile + RESET);
                                }
                            });
                }

                Map<String, List<String>> contents = new HashMap<>();
                contents.put("classes", libraryInterfaces);
                contents.put("interfaces", libraryClasses);
                libraryContents.put(libName, contents);
                System.out.println(CYAN + "Processed " + libName + " (" + libraryClasses.size() + " classes, " + YELLOW + libraryInterfaces.size() + " interfaces" + CYAN + ")" + RESET);
            }
            writeReflectConfig(classes, constructOutputFilePath(reflectConfigFile));
            writeProxyConfig(interfaces, constructOutputFilePath(proxyConfigFile));
            System.out.println(GREEN + "\nTotal libraries found: " + libraries.size() + RESET);
            System.out.println(GREEN + "Total libraries' classes found: " + classes.size() + RESET);
            System.out.println(GREEN + "Total libraries' interfaces found: " + interfaces.size() + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error analyzing JAR: " + e.getMessage() + RESET);
        }

        return libraryContents;
    }


    // Write to file
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
                ArrayNode arrayNode = mapper.createArrayNode();
                arrayNode.add(fullClassName);
                classNode.put("interfaces", arrayNode);
                proxyConfig.add(classNode);
            }
            mapper.writeValue(new File(proxyConfigPath.toString()), proxyConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Validation
    public static void pathValidation () {
        try {
            Map<String, Path> pathMap = Map.of(
                    "projectPath", projectPath,
                    "parentOutputPath", parentOutputPath
            );
            Map<String, Path> filePathMap = Map.of(
                    "reflectConfigFile", reflectConfigFile,
                    "proxyConfigFile", proxyConfigFile
            );

            Map<String, String> validPaths = new HashMap<>();
            Map<String, String> validFilePaths = new HashMap<>();
            Map<String, String> invalidPaths = new HashMap<>();
            Map<String, String> invalidFilePaths = new HashMap<>();

            pathMap.forEach((key, path) -> {
                if (!Files.exists(path) || !Files.isDirectory(path) || path.toString().isEmpty()){
                    invalidPaths.put(key, path.toString());
                } else {
                    validPaths.put(key, path.toString());
                }
            });

            filePathMap.forEach((key, path) -> {
                Path filePath = constructOutputFilePath(path);
                File file = new File(filePath.toString());
                if (!file.exists()){
                    invalidFilePaths.put(key, filePath.toString());
                } else {
                    validFilePaths.put(key, filePath.toString());
                }
            });
            invalidPaths.forEach((key, pathValue) -> { System.out.println(ConsoleColors.RED + "Invalid path : " + RESET + ConsoleColors.PURPLE + key + RESET + " -> " + pathValue); });

            validPaths.forEach((key, pathValue) -> { System.out.println(GREEN + "Valid path : " + RESET + ConsoleColors.PURPLE + key + RESET + " -> " + pathValue); });

            invalidFilePaths.forEach((key, pathValue) -> { System.out.println(ConsoleColors.RED + "Invalid path : " + RESET + ConsoleColors.PURPLE + key + RESET + " -> " + pathValue); });

            validFilePaths.forEach((key, pathValue) -> { System.out.println(GREEN + "Valid path : " + RESET + ConsoleColors.PURPLE + key + RESET + " -> " + pathValue); });

            if (invalidPaths.size() > 0){ System.exit(1); }
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Something was wrong -> " + e.getMessage() + RESET);
        }
    }
    public static void buildFatJar () {
        File workingDir = projectPath.toFile();
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        String mvnCommand = isWindows ? "mvn.cmd" : "mvn";

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(mvnCommand, "clean", "package", "-DskipTests");
        processBuilder.directory(workingDir);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(ConsoleColors.GREEN_BACKGROUND + "BUILD SUCCESS" + RESET);
            } else {
                System.out.println(ConsoleColors.RED + "BUILD FAILURE" + RESET);
                System.out.println("Exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Construction
    public static Path constructOutputFilePath(Path fileName) {
        Path result = Paths.get("");
        try {
            result = Paths.get(parentOutputPath + "\\" + fileName);
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Something was wrong while constructing file path -> " + e.getMessage() + RESET);
        }
        return result;
    }

    public static String constructJarFileName () {
        String result = "";
        try {
            Path pomPath = Paths.get(projectPath.toString()+ "\\pom.xml");
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(String.valueOf(pomPath)));
            result = model.getArtifactId() + "-" + model.getVersion() + ".jar";
            System.out.println("Constructed JAR file name : " + result);
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Something was wrong while constructing jar file name -> " + e.getMessage() + RESET);
        }
        return result;
    }

    public static String constructJarFilePath (String jarFileName) {
        String result = "";
        try {
            result = projectPath + "\\target\\"+ jarFileName;
            System.out.println("Constructed JAR file path : " + result + "\n");
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Something was wrong while constructing jar file path -> " + e.getMessage() + RESET);
        }
        return result;
    }
}
