import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.modrinth.com/v2/version/oYfJQ6lR")
                .build();

        Response response = client.newCall(request).execute();

        ModrinthVersion version = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), ModrinthVersion.class);

        System.out.println(version.getName());
    }
}
