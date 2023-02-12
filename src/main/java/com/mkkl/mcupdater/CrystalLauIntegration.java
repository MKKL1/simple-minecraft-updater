package com.mkkl.mcupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CrystalLauIntegration {
    private String instancesDir;

    public void findInstancesDir() {

    }

    public void setInstancesDir(String instancesDir) {
        this.instancesDir = instancesDir;
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
}
