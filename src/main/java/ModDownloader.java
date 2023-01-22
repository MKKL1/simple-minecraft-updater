import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ModDownloader {

    private final OkHttpClient client;

    public ModDownloader(OkHttpClient client) {
        this.client = client;
    }

    public CompletableFuture<byte[]> downloadFileAsync(final String downloadUrl) throws MalformedURLException {
        return downloadFileAsync(new URL(downloadUrl));
    }

    public CompletableFuture<byte[]> downloadFileAsync(final URL downloadUrl) {
        CompletableFuture<byte[]> completableFuture = new CompletableFuture<>();
        Request request = new Request.Builder().url(downloadUrl).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                completableFuture.completeExceptionally(e);
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    completableFuture.completeExceptionally(new IOException("Failed to download file: " + response));
                }
                completableFuture.complete(Objects.requireNonNull(response.body()).bytes());
            }
        });
        return completableFuture;
    }
}
