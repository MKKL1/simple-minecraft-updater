import okhttp3.*;
import okio.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FileDownloader {

    private final OkHttpClient client;

    public FileDownloader(OkHttpClient client) {
        this.client = client;
    }

    public CompletableFuture<Response> downloadFileAsync(final String downloadUrl) throws MalformedURLException {
        return downloadFileAsync(new URL(downloadUrl));
    }

    public CompletableFuture<Response> downloadFileAsync(final URL downloadUrl) {
        CompletableFuture<Response> completableFuture = new CompletableFuture<>();
        Request request = new Request.Builder().url(downloadUrl).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                completableFuture.completeExceptionally(e);
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (!response.isSuccessful()) {
                    completableFuture.completeExceptionally(new IOException("Failed to download file: " + response));
                }
                completableFuture.complete(Objects.requireNonNull(response));
            }
        });
        return completableFuture;
    }
}
