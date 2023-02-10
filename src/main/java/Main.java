import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        final ProgressListener progressListener = new ProgressListener() {
            @Override public void update(long bytesRead, long contentLength, boolean done) {
                if (done) {
                    System.out.println("completed");
                }
            }
        };
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                })
                .build();
        ModList modList;
        try(FileReader fileReader = new FileReader("mods.json")) {
            modList = mapper.readValue(fileReader, ModList.class);
        }

        ModUpdater modUpdater = new ModUpdater(client);
        modUpdater.updateMods(modList);


        try(FileWriter fileWriter = new FileWriter("mods.json")) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, modList);
        }

        client.connectionPool().evictAll();
    }

}
