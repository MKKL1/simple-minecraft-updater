import modrinth.ListModData;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FilenameUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ModInstaller {
    private final String modsDirectory;
    private final FileDownloader modDownloader;

    public ModInstaller(OkHttpClient client, String modsDirectory) {
        this.modsDirectory = modsDirectory;
        modDownloader = new FileDownloader(client);
    }

    public void installMods(ModList modList) {
        ArrayList<CompletableFuture<String>> completableFutureList = new ArrayList<CompletableFuture<String>>();
        for(ListModData modData : modList.getMods()) {
            completableFutureList.add(installMod(modData));
        }
        completableFutureList.forEach(x -> x.exceptionallyAsync(throwable -> {
            throwable.printStackTrace();
            return null;
        }));
        completableFutureList.forEach(CompletableFuture::join);
    }

    public CompletableFuture<String> installMod(ListModData modData) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        try {
            URL modUrl = new URL(modData.getFile_url());
            modDownloader.downloadFileAsync(modUrl).thenAcceptAsync(response -> {
                String pathToDownload = URLDecoder.decode(String.valueOf(Path.of(modsDirectory, FilenameUtils.getName(modUrl.getPath()))), StandardCharsets.UTF_8);

                byte[] modbody = new byte[0];
                try {
                    modbody = Objects.requireNonNull(response.body()).bytes();
                    if(modData.getJar_mod_id() == null) {
                        ModJarReader modJarReader = ModJarReader.create(modbody);
                        modData.setJar_mod_id(modJarReader.getFabricModJson().id);
                    }
                } catch (IOException e) {
                    completableFuture.completeExceptionally(e);
                }



                try(FileOutputStream fos = new FileOutputStream(pathToDownload)) {
                    fos.write(Objects.requireNonNull(modbody));
                    completableFuture.complete(pathToDownload);
                } catch (IOException e) {
                    completableFuture.completeExceptionally(e);
                } finally {
                    Objects.requireNonNull(response.body()).close();
                }
            });
        } catch (MalformedURLException e) {
            completableFuture.completeExceptionally(e);
        }
        return completableFuture;
    }
}
