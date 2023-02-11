import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class ModUpdater {
    ObjectMapper mapper;
    OkHttpClient client;
    private final String modDirectory;

    public ModUpdater(OkHttpClient client, String modDirectory) {
        this.client = client;
        this.modDirectory = modDirectory;
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }

    public void verifyModList(ModList modList) throws IOException {
        ModInstaller modInstaller = new ModInstaller(client, modDirectory);
        for(ListModData modData : modList.getMods()) {
            if(modData.getJar_mod_id() == null || modData.getSha512() == null) {
                modInstaller.downloadMod(modData).thenAccept(response1 -> {
                    try {
                        byte[] bytes = Objects.requireNonNull(response1.body()).bytes();
                        ModJarReader modJarReader = ModJarReader.create(bytes);
                        modData.setFabricModJson(modJarReader.getFabricModJson());
                        modData.setSha512(DigestUtils.sha512Hex(new ByteArrayInputStream(bytes)));
                        response1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).exceptionally(ex -> {
                    System.out.println("Error on " + modData.getMod_name() + ":" + ex.getMessage());
                    return null;
                }).join();
            }
        }
    }

    public List<ListModData> getModsToUpdate(ModList modList) throws IOException {
        List<ListModData> modsToUpdate = new ArrayList<>(modList.getMods());

        System.out.println("Checking mod dictionary...");
        try (Stream<Path> paths = Files.walk(Paths.get(modDirectory), 1)) {
            for(Path path : paths.toList()) {
                try {
                    if (!Files.isRegularFile(path) || !path.toString().toLowerCase(Locale.ROOT).endsWith(".jar")) continue;
                    File modFile = path.toFile();
                    System.out.println("Scanning " + modFile.getName());
                    try(FileInputStream fileInputStream = new FileInputStream(modFile)) {
                        byte[] bytes = fileInputStream.readAllBytes();
                        //Create jar reader of file from mods dictionary
                        ModJarReader modJarReader = ModJarReader.create(bytes);
                        //Get id of mod from jar file
                        String id = modJarReader.getFabricModJson().id;
                        //System.out.println(modFile.getName() + " is a fabric mod with id of " + id);
                        //Find corresponding mod data
                        ListModData modData = modList.getMods().stream().filter(x -> x.getJar_mod_id().equals(id)).findFirst().orElse(null);
                        if(modData == null) {
                            System.out.println(modFile.getName() + " is not in update list");
                            continue;
                        }

                        //Check hash of jar file
                        String hash = DigestUtils.sha512Hex(bytes);
                        //Compare hash of jar file to saved hash in mod data
                        if(hash.equals(modData.getSha512())) {
                            modsToUpdate.remove(modData);
                            continue;
                        }
                    }


                    System.out.println("Added " + modFile.getName() + " to update list");
                } catch (IOException e) {
                    System.out.println("Failed to read data of " + path.getFileName());
                }
            }
        }
        if(modsToUpdate.isEmpty()) {
            System.out.println("No mods to update");
            return null;
        }
        System.out.println("Updating mods: ");
        for(ListModData modData : modsToUpdate) {
            System.out.println("+ " + modData.getMod_name() + " : " + modData.getVersion_number());
        }
        return modsToUpdate;
    }
}
