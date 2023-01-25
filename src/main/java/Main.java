import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import modrinth.ModData;
import modrinth.ModrinthVersion;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {

        ModList modList;
        try(FileReader fileReader = new FileReader("mods.json")){
            modList = mapper.readValue(fileReader, ModList.class);
        }


        try(FileWriter fileWriter = new FileWriter("mods.json")) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, modList);
        }

    }

}
