import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import modrinth.ListModData;
import modrinth.ModrinthVersion;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class ModUpdater {
    ObjectMapper mapper;
    OkHttpClient client;
    public boolean checkFabricModId = true;
    private final String modDirectory;
    private final ModInstaller modInstaller;

    public ModUpdater(OkHttpClient client, String modDirectory) {
        this.client = client;
        this.modDirectory = modDirectory;
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        modInstaller = new ModInstaller(client, modDirectory);
    }

    public void verifyModList(ModList modList) throws IOException {

        for(ListModData modData : modList.getMods()) {
            Request request;
            Response response;
            if(modData.getVersion_id() == null) {
                request = new Request.Builder()
                        .url("https://api.modrinth.com/v2/project/" + modData.getMod_name() + "/version")
                        .build();
                response = client.newCall(request).execute();
                List<ModrinthVersion> list = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), new TypeReference<>() {});
                modData.setVersion_id(list.stream().filter(x -> x.getVersion_number().equals(modData.getVersion_number())).findFirst().orElseThrow().getId());
            }

            //Modrinth api get version
            request = new Request.Builder()
                    .url("https://api.modrinth.com/v2/version/" + modData.getVersion_id())
                    .build();
            response = client.newCall(request).execute();

            ModrinthVersion version = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), ModrinthVersion.class);
            modData.updateData(version);

            if(checkFabricModId && modData.getJar_mod_id() == null) {
                modInstaller.downloadMod(modData).thenAccept(response1 -> {
                    try {
                        ModJarReader modJarReader = ModJarReader.create(Objects.requireNonNull(response1.body()).bytes());
                        String id = modJarReader.getFabricModJson().id;
                        modData.setJar_mod_id(id);
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

    public void updateMods(ModList modList) throws IOException {
        List<ListModData> modsToUpdate = new ArrayList<>(modList.getMods());

        System.out.println("Checking mod dictionary...");
        try (Stream<Path> paths = Files.walk(Paths.get(modDirectory), 1)) {
            for(Path path : paths.toList()) {
                try {
                    if (!Files.isRegularFile(path) || !path.toString().toLowerCase(Locale.ROOT).endsWith(".jar")) continue;
                    File modFile = path.toFile();
                    System.out.println("Scanning " + modFile.getName());
                    //Create jar reader of file from mods dictionary
                    ModJarReader modJarReader = ModJarReader.create(new FileInputStream(modFile));
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
                    String hash = DigestUtils.sha512Hex(new FileInputStream(modFile));
                    //Compare hash of jar file to saved hash in mod data
                    if(hash.equals(modData.getSha512())) {
                        modsToUpdate.remove(modData);
                        continue;
                    }
                    System.out.println("Added " + modFile.getName() + " to update list");
                } catch (IOException e) {
                    System.out.println("Failed to read data of " + path.getFileName());
                }
            }
        }
        if(modsToUpdate.isEmpty()) {
            System.out.println("No mods to update");
            return;
        }
        System.out.println("Updating mods: ");
        for(ListModData modData : modsToUpdate) {
            System.out.println("+ " + modData.getMod_name() + " : " + modData.getVersion_number());
        }

        modInstaller.installMods(modsToUpdate);
    }
}
