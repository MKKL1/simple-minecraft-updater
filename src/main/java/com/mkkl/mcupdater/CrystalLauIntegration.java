package com.mkkl.mcupdater;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CrystalLauIntegration {
    private String instancesDir;

    public static String findInstancesDir() {
        return Path.of(System.getProperty("user.home"),"AppData", "Roaming", "Crystal-Launcher", "instances").toAbsolutePath().toString();
    }

    public void setInstancesDir(String instancesDir) {
        this.instancesDir = instancesDir;
    }

    public String getInstancesDir() {
        return instancesDir;
    }

    public Path createEmptyInstance(String instanceName, String fbcmlversion) throws IOException {
        Path instancePath = Path.of(instancesDir, "u." + instanceName);
        Files.createDirectories(instancePath);
        try(FileWriter fileWriter = new FileWriter(Path.of(instancePath.toString(), ".fbcmlversion").toString())) {
            fileWriter.write(fbcmlversion);
        }
        Path modsPath = Path.of(instancePath.toString(), ".minecraft", "mods");
        Files.createDirectories(modsPath);
        return modsPath;
    }

    public Path getInstanceModPath(String instanceName) {
        return Path.of(instancesDir, "u." + instanceName, ".minecraft", "mods");
    }

    public List<String> getListOfInstaces() {
        return Arrays.stream(Objects.requireNonNull(new File(instancesDir).list((current, name) -> new File(current, name).isDirectory()))).toList();
    }
}
