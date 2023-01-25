import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import modrinth.ModData;
import modrinth.ModrinthVersion;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ModUpdater {
    ObjectMapper mapper;
    OkHttpClient client;

    public ModUpdater(OkHttpClient client) {
        this.client = client;
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }

    public void updateMods(ModList modList) {
        for(ModData modData : modList.getMods()) {
            Request request;
            Response response;
            if(modData.getVersion_id() == null) {
                request = new Request.Builder()
                        .url("https://api.modrinth.com/v2/project/" + modData.getMod_name() + "/version")
                        .build();
                response = client.newCall(request).execute();
                List<ModrinthVersion> list = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), new TypeReference<>() {});
                System.out.println(list.toString());
                modData.setVersion_id(list.stream().filter(x -> x.getVersion_number().equals(modData.getVersion_number())).findFirst().orElseThrow().getId());
            }

            request = new Request.Builder()
                    .url("https://api.modrinth.com/v2/version/" + modData.getVersion_id())
                    .build();
            response = client.newCall(request).execute();

            ModrinthVersion version = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), ModrinthVersion.class);
            modData.updateData(version);

            System.out.println(version);
        }

        ModInstaller modInstaller = new ModInstaller(client, "mods");
        modInstaller.installMods(modList);
    }
}
