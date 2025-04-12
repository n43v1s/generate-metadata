package org.example.generatemetadata.experimentalv2;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationVariables {
    /**
     * Properties
     * */
    public static String projectName = "credit-balance-history";
    public static String excludedImportPrefix = "import id.co.xl."+ projectName +".";
    public static String version = "1.0.0-Alpha";
    public static boolean includeSpring = false;
    public static String directorySlash = "\\";
    static int count = 0;
    public static Path logoPath = Paths.get("C:\\Users\\agusilaban\\xl\\generate-metadata\\src\\main\\resources\\banner.txt");


    /**
     * Input
     * */
    public static Path projectPath = Paths.get("C:\\Users\\agusilaban\\xl\\" + projectName);
    public static Path repositoryPath = Paths.get("C:\\Users\\agusilaban\\.m2\\repository\\");
    public static Path dependenciesListTxtPath = Paths.get(projectPath + "\\dependencies.txt");


    /**
     * Output
     * */
    public static Path outputPath = Paths.get("C:\\Users\\agusilaban\\Downloads\\agus\\experimental");

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
     * Final Output
     * */
    public static Path reflectConfigFile = Paths.get("reflect-config.json");
    public static Path proxyConfigFile = Paths.get("proxy-config.json");
}

