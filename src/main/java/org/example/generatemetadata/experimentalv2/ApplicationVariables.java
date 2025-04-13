package org.example.generatemetadata.experimentalv2;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.generatemetadata.experimental.ConsoleColors.PURPLE;
import static org.example.generatemetadata.experimental.ConsoleColors.RESET;

public class ApplicationVariables {
    /**
     * Properties
     * */
    public static String projectName = "direct-debit-account";
    public static String version = "1.0.0-Alpha";
    public static boolean includeSpring = false;
    public static String directorySlash = "\\";
    static int count = 0;
    public static String os = System.getProperty("os.name").toLowerCase();
    public static boolean isWindows = os.contains("win");
    public static String mvnCommand = isWindows ? "mvn.cmd" : "mvn";
    public static String projectArtifactId;
    public static String projectGroupId;
    public static String projectVersion;
    public static String excludedImportPrefix;
    public static String fatJarName;
    public static String fatJarFilePath;


    /**
     * Paths
     * */
    public static Path projectPath = Paths.get("C:\\Users\\agusilaban\\xl\\" + projectName);
    public static Path repositoryPath = Paths.get("C:\\Users\\agusilaban\\.m2\\repository");
    public static Path outputPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\complete");


    /**
     * File Name
     * */
    // Maven Dependency List
    public static String mavenDependenciesListTxt = "dependencies.txt";
    public static Path projectPomPath = projectPath.resolve("pom.xml");

    // Reflect Config
    public static Path projectReflectionPath = outputPath.resolve("project-reflect-config.json");
    public static Path importReflectionPath = outputPath.resolve("import-reflect-config.json");
    public static Path pomReflectionPath = outputPath.resolve("pom-reflect-config.json");
    public static Path dependenciesReflectionPath = outputPath.resolve("dependency-reflect-config.json");
    public static Path fatJarReflectionPath = outputPath.resolve("fatjar-reflect-config.json");

    //Proxy Config
    public static Path projectProxyPath = outputPath.resolve("project-proxy-config.json");
    public static Path importProxyPath = outputPath.resolve("import-proxy-config.json");
    public static Path pomProxyPath = outputPath.resolve("pom-proxy-config.json");
    public static Path dependenciesProxyPath = outputPath.resolve("dependency-proxy-config.json");
    public static Path fatJarProxyPath = outputPath.resolve("fatjar-proxy-config.json");


    /**
     * Output
     * */
    public static Path reflectConfigFile = outputPath.resolve("reflect-config.json");
    public static Path proxyConfigFile = outputPath.resolve("proxy-config.json");


    public static void constructProjectGroupIdAndArtifactId () {
        try {
            Path pomPath = Paths.get(String.valueOf(projectPomPath));
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(String.valueOf(pomPath)));
            projectArtifactId = model.getArtifactId();
            projectGroupId = model.getGroupId();
            projectVersion  = model.getVersion();
            fatJarName = projectArtifactId + "-" + projectVersion + ".jar";
            fatJarFilePath = projectPath + "\\target\\" + fatJarName;
            excludedImportPrefix = "import " + projectGroupId + "." + constructProjectMainDir().values().toArray()[0];
            System.out.println(PURPLE + "Project Name" + RESET + " -> " + projectName);
            System.out.println(PURPLE + "Artifact Id" + RESET + " -> " + projectArtifactId);
            System.out.println(PURPLE + "Group Id" + RESET + " -> " + projectGroupId);
            System.out.println(PURPLE + "Excluded Import Prefix" + RESET + " -> " + excludedImportPrefix + "\n");
        } catch (Exception e) {
            System.out.println("Error reading POM file: " + e.getMessage());
        }
    }
    public static Map<String, String> constructProjectMainDir () {
        Map<String, String> result = new HashMap<>();
        try {
            Path path1 = Paths.get(
                    projectPath +
                            "\\src\\main\\java\\" +
                            projectGroupId.replace(".", directorySlash) +
                            directorySlash +
                            projectName
            );
            Path path2 = Paths.get(
                    projectPath +
                    "\\src\\main\\java\\" +
                    projectGroupId.replace(".", directorySlash) +
                    directorySlash +
                    projectName.replace("-", "")
                    );
            Path path3 = Paths.get(
                    projectPath +
                            "\\src\\main\\java\\" +
                            projectGroupId.replace(".", directorySlash) +
                            directorySlash +
                            projectName.replace("-", directorySlash)
            );

            if (Files.isDirectory(path1)) {
                result.put(String.valueOf(path1), projectName);
            } else if (Files.isDirectory(path2)) {
                result.put(String.valueOf(path2), projectName.replace("-", ""));
            } else if (Files.isDirectory(path3)) {
                result.put(String.valueOf(path2), projectName.replace("-", "."));
            } else {
                result = findMainClass();
            }
        } catch (Exception e) {
            System.out.println("Error constructing project main directory: " + e.getMessage());
        }
        return result;
    }

    public static Map<String, String>findMainClass () {
        Map<String, String> result = new HashMap<>();
        try {
            Files.walk(projectPath)
                    .filter(path -> path.toString().endsWith("Application.java"))
                    .forEach(path -> {
                        try {
                            String packageName = "";
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                line = line.trim();
                                if (line.startsWith("package")) {
                                    packageName = line
                                            .replace("package ", "")
                                            .replace(";", "")
                                            .replace(projectGroupId, "");
                                }
                            }
                            Path fixedPath = Paths.get(
                                    projectPath +
                                            "\\src\\main\\java\\" +
                                            projectGroupId.replace(".", directorySlash) +
                                            packageName.replace(".", directorySlash)
                            );
                            result.put(String.valueOf(fixedPath), packageName.replace(".", ""));
                        } catch (IOException ioe) {
                            System.out.println("Error finding main class: " + ioe.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.out.println("Error finding main class: " + e.getMessage());
        }
        return result;
    }
}


