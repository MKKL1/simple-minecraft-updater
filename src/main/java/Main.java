import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        OkHttpClient client = new OkHttpClient();
        ModList modList;


        try(FileReader fileReader = new FileReader("mods.json")) {
            modList = mapper.readValue(fileReader, ModList.class);
        }

        ModUpdater modUpdater = new ModUpdater(client, "mods");
        modUpdater.verifyModList(modList);
        modUpdater.updateMods(modList);


        try(FileWriter fileWriter = new FileWriter("mods.json")) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, modList);
        }

        client.dispatcher().executorService().shutdown();
    }

}
