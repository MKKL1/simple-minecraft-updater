import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        OkHttpClient client = new OkHttpClient();
        ModList modList;
        try(FileReader fileReader = new FileReader("mods.json")){
            modList = mapper.readValue(fileReader, ModList.class);
        }
        for(ModData modData : modList.getMods()) {
            Request request = new Request.Builder()
                    .url("https://api.modrinth.com/v2/version/" + modData.getVersion_id())
                    .build();

            Response response = client.newCall(request).execute();

            ModrinthVersion version = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), ModrinthVersion.class);
            modData.updateData(version);
            if(modData.getVersion_id() == null) {
                request = new Request.Builder()
                        .url("https://api.modrinth.com/v2/project/" + modData.getMod_name() + "/version")
                        .build();
                response = client.newCall(request).execute();
                List<ModrinthVersion> list = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), new TypeReference<>() {});
                modData.setVersion_id(list.stream().filter(x -> x.getVersion_number() == modData.getVersion_number()).findFirst().orElseThrow().getId());
            }
            System.out.println(version);
        }

//        ModInstaller modInstaller = new ModInstaller(client, "mods");
//        modInstaller.installMods(modList);

        try(FileWriter fileWriter = new FileWriter("mods.json")) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, modList);
        }

    }

}
