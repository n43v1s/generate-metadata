package org.example.generatemetadata.experimentalv2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.example.generatemetadata.experimental.ConsoleColors.*;
import static org.example.generatemetadata.experimentalv2.ApplicationVariables.*;

public class GenerateMetadata {
    public static void main(String[] args) {
        printLogo();
        isAllPathsValid();
        listAllDependencies();
        buildFatJar();

        scanProject();
        scanProjectImports();
        scanProjectPom();
        scanProjectDependencies();
        scanProjectFatJar();

        configureReflectConfig();
        configureProxyConfig();
        System.out.println("Finished Generating Metadata");
    }

    public static void isAllPathsValid () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting path validation");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during path validation: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Path validation complete\n");
    }

    public static void scanProject () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project scan finished\n");
    }

    public static void scanProjectImports () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project imports scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project imports scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project imports scan finished\n");
    }

    public static void scanProjectPom () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project POM scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project POM scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project pom POM finished\n");
    }

    public static void scanProjectDependencies () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project dependencies scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project dependencies scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project dependencies scan finished\n");
    }

    public static void scanProjectFatJar () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Starting project fat jar scan");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred during project fat jar scan: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Project fat jar scan finished\n");
    }

    public static void listAllDependencies () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Listing all project dependencies");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while listing project dependencies: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished listing all project dependencies\n");
    }

    public static void buildFatJar () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Building project fat jar");
        try {

        } catch (Exception e){
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while building project fat jar: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished Building project fat jar\n");
    }

    public static void configureReflectConfig () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Configuring reflection metadata");
        try {

        } catch (Exception e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while configuring reflection metadata: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished configuring reflection metadata\n");
    }

    public static void configureProxyConfig () {
        System.out.println(BLUE + "[RUNNING] \t " + RESET + "Configuring proxy metadata");
        try {

        } catch (Exception e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "An error occurred while configuring proxy metadata: " + e.getMessage());
        }
        System.out.println(GREEN + "[COMPLETE] \t " + RESET + "Finished configuring proxy metadata\n");
    }

    public static void writeReflectConfig () {}

    public static void writeProxyConfig () {}

    public static void printLogo() {
        try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(logoPath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(WHITE + line + RESET);
            }
            System.out.println("Metadata Generator Version : " + version);
            System.out.println("Project Name : " + projectName + "\n");
        } catch (IOException e) {
            System.out.println(RED + "[ERROR] \t " + RESET + "Unable to load logo: " + e.getMessage());
        }
    }
}
