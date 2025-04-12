package org.example.generatemetadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

public class GenerateLibraryMetadataApplication {
    public static void main(String[] args) {
        String jarPath = "C:\\Users\\agusilaban\\.m2\\repository\\id\\co\\xl\\lib-cloudconfig\\0.0.1\\lib-cloudconfig-0.0.1.jar";
        String outputReflectConfig = "C:\\Users\\agusilaban\\xl\\metadatas\\META-INF\\native-image\\reflect-config.json";

//        processLibJar(jarPath);
        processLibJarV2(jarPath);
//        processLibJarV3(jarPath, libDir);
    }

    public static void processLibJar(String jarPath) {
        try (JarFile jarFile = new JarFile(new File(jarPath))) {
            Enumeration<JarEntry> entries = jarFile.entries();

            System.out.println("Classes in " + jarPath + ":");
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.replace("/", ".").replace(".class", "");
                    System.out.println(className);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processLibJarV2(String jarPath) {
        try (JarFile jarFile = new JarFile(new File(jarPath))) {
            Enumeration<JarEntry> entries = jarFile.entries();

            URL jarURL = new File(jarPath).toURI().toURL();
            try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL}, Thread.currentThread().getContextClassLoader())) {
                System.out.println("Classes in " + jarPath + ":");
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.endsWith(".class")) {
                        String className = name.replace("/", ".").replace(".class", "");
                        try {
                            Class<?> loadedClass = classLoader.loadClass(className);
                            System.out.println("\nClass: " + loadedClass.getName());

                            Method[] methods = loadedClass.getDeclaredMethods();
                            for (Method method : methods) {
                                Parameter[] parameters = method.getParameters();
                                StringBuilder paramList = new StringBuilder();
                                for (int i = 0; i < parameters.length; i++) {
                                    paramList.append(parameters[i].getType().getSimpleName()) // Get parameter type
                                            .append(" ")
                                            .append(parameters[i].getName()); // Get parameter name
                                    if (i < parameters.length - 1) {
                                        paramList.append(", ");
                                    }
                                }
                                System.out.println("  - " + method.getName() + " -> params: [" + paramList + "]");
                            }
                        } catch (Throwable e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processLibJarV3(String jarPath, String libDir) {
        try (JarFile jarFile = new JarFile(new File(jarPath))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            URL[] urls = getDependencyJars(libDir);
            try (URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader())) {
                System.out.println("Classes in " + jarPath + ":");
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        String className = name.replace("/", ".").replace(".class", "");
                        try {
                            Class<?> loadedClass = classLoader.loadClass(className);
                            System.out.println("\nClass: " + loadedClass.getName());
                            Method[] methods = loadedClass.getDeclaredMethods();
                            for (Method method : methods) {
                                Parameter[] parameters = method.getParameters();
                                StringBuilder paramList = new StringBuilder();
                                for (int i = 0; i < parameters.length; i++) {
                                    paramList.append(parameters[i].getType().getSimpleName()).append(" ").append(parameters[i].getName());
                                    if (i < parameters.length - 1) {
                                        paramList.append(", ");
                                    }
                                }
                                System.out.println("  - " + method.getName() + " -> params: [" + paramList + "]");
                            }
                        } catch (Exception e) {
                            System.err.println("Could not load class: " + className + " - " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static URL[] getDependencyJars(String libDir) throws IOException {
        File libFolder = new File(libDir);
        List<URL> jarUrls = new ArrayList<>();
        if (libFolder.exists() && libFolder.isDirectory()) {
            File[] files = libFolder.listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null) {
                for (File jar : files) {
                    jarUrls.add(jar.toURI().toURL());
                }
            }
        }
        return jarUrls.toArray(new URL[0]);
    }

    private static void addClassToReflectConfig(ArrayNode proxyConfig, String fullClassName) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode classNode = mapper.createObjectNode();
        classNode.put("name", fullClassName);
        classNode.put("allDeclaredFields", true);
        classNode.put("allDeclaredMethods", true);
        classNode.put("allDeclaredConstructors", true);
        proxyConfig.add(classNode);
    }
}
