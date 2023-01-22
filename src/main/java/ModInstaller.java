import okhttp3.OkHttpClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModInstaller {
    private final String modsDirectory;
    private final ModDownloader modDownloader;

    public ModInstaller(OkHttpClient client, String modsDirectory) {
        this.modsDirectory = modsDirectory;
        modDownloader = new ModDownloader(client);
    }

    public void installMods(ModList modList) {
        ArrayList<CompletableFuture<String>> completableFutureList = new ArrayList<CompletableFuture<String>>();
        for(ModData modData : modList.getMods()) {
            completableFutureList.add(installMod(modData));
        }
        completableFutureList.forEach(x -> x.exceptionallyAsync(throwable -> {
            throwable.printStackTrace();
            return null;
        }));
        completableFutureList.forEach(CompletableFuture::join);
    }

    public CompletableFuture<String> installMod(ModData modData) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        try {
            URL modUrl = new URL(modData.getFile_url());
            modDownloader.downloadFileAsync(modUrl).thenAcceptAsync(bytes -> {
                String pathToDownload = URLDecoder.decode(String.valueOf(Path.of(modsDirectory, FilenameUtils.getName(modUrl.getPath()))), StandardCharsets.UTF_8);
                try(FileOutputStream fos = new FileOutputStream(pathToDownload)) {
                    fos.write(bytes);
                    completableFuture.complete(pathToDownload);
                } catch (IOException e) {
                    completableFuture.completeExceptionally(e);
                }
            });
        } catch (MalformedURLException e) {
            completableFuture.completeExceptionally(e);
        }
        return completableFuture;
    }
}
