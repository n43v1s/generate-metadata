package org.example.generatemetadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateMetadataApplication {

	public static void main(String[] args) throws IOException {
		String projectDir = "C:\\Users\\agusilaban\\xl\\recharge-history";
		String outputReflectConfig = "C:\\Users\\agusilaban\\xl\\metadatas\\META-INF\\native-image\\reflect-config.json";
		String outputProxyConfig = "C:\\Users\\agusilaban\\xl\\metadatas\\META-INF\\native-image\\proxy-config.json";
		List<Path> javaFiles;

		try (Stream<Path> paths = Files.walk(Paths.get(projectDir))) {
			javaFiles = paths
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".java"))
					.collect(Collectors.toList());
		}
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode proxyConfig = mapper.createArrayNode();
		ArrayNode reflectConfig = mapper.createArrayNode();

		for (Path javaFile : javaFiles) {
			processJavaFile(javaFile, reflectConfig);
			processJavaInterfaceFile(javaFile, proxyConfig);
		}
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputReflectConfig), reflectConfig);
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputProxyConfig), proxyConfig);

		System.out.println("reflect-config.json generated at: " + outputReflectConfig);
		System.out.println("proxy-config.json generated at: " + outputProxyConfig);
	}

	private static void processJavaFile(Path javaFile, ArrayNode reflectConfig) throws IOException {
		List<String> lines = Files.readAllLines(javaFile);
		String packageName = null;
		String className = null;
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith("package ")) {
				packageName = line.split(" ")[1].replace(";", "");
			}
			if (line.startsWith("public class ") || line.startsWith("class ")) {
				line = line.replace("{", "");
				String[] parts = line.split(" ");
				if (parts.length >= 3) {
					className = parts[2];
				} else if (parts.length == 2) {
					className = parts[1];
				}
			}
			if (line.startsWith("public static class ") || line.startsWith("static class ")) {
				line = line.replace("{", "");
				String[] parts = line.split(" ");
				if (parts.length >= 4 && packageName != null && className != null) {
					String innerClassName = parts[3];
					addClassToReflectConfig(reflectConfig, removeGenerics(packageName + "." + className + "$" + innerClassName));
				}
			}
		}
		if (packageName != null && className != null) {
			addClassToReflectConfig(reflectConfig, removeGenerics(packageName + "." + className));
		}
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

	private static void processJavaInterfaceFile(Path javaFile, ArrayNode proxyConfig) throws IOException {
		List<String> lines = Files.readAllLines(javaFile);
		String packageName = null;
		String className = null;
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith("package ")) {
				packageName = line.split(" ")[1].replace(";", "");
			}

			if (line.startsWith("public interface ") || line.startsWith("interface ")) {
				line = line.replace("{", "");
				String[] parts = line.split(" ");
				if (parts.length >= 3) {
					className = parts[2];
				} else if (parts.length == 2) {
					className = parts[1];
				}
			}

			if (line.startsWith("public static interface ") || line.startsWith("static interface ")) {
				line = line.replace("{", "");
				String[] parts = line.split(" ");
				if (parts.length >= 4 && packageName != null && className != null) {
					String innerClassName = parts[3];
					addClassToProxyConfig(proxyConfig, removeGenerics(packageName + "." + className + "$" + innerClassName));
				}
			}
		}

		// Add the main class to the config if applicable
		if (packageName != null && className != null) {
			addClassToProxyConfig(proxyConfig, removeGenerics(packageName + "." + className));
		}
	}

	private static void addClassToProxyConfig(ArrayNode reflectConfig, String fullClassName) {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode arrayNode = mapper.createArrayNode();
		arrayNode.add(fullClassName);
		ObjectNode classNode = mapper.createObjectNode();
		classNode.put("interfaces", arrayNode);
		reflectConfig.add(classNode);
	}

	private static String removeGenerics(String className) {
		return className.replaceAll("<.*?>", "");
	}

}
