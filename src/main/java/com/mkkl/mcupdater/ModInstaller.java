package com.mkkl.mcupdater;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ModInstaller {
    private final String modsDirectory;
    private final FileDownloader modDownloader;

    public ModInstaller(OkHttpClient client, String modsDirectory) {
        this.modsDirectory = modsDirectory;
        modDownloader = new FileDownloader(client);
    }

//    public void installMods(List<ListModData> modList) {
//        ArrayList<CompletableFuture<String>> completableFutureList = new ArrayList<CompletableFuture<String>>();
//        for(ListModData modData : modList) {
//            completableFutureList.add(installMod(modData));
//        }
//        completableFutureList.forEach(x -> x.exceptionallyAsync(throwable -> {
//            throwable.printStackTrace();
//            return null;
//        }));
//        completableFutureList.forEach(CompletableFuture::join);
//    }

    public CompletableFuture<String> installMod(AddUpdateGoal updateGoal) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        try {
            URL modUrl = new URL(updateGoal.modData.getFile_url());
            String pathToDownload = URLDecoder.decode(String.valueOf(Path.of(modsDirectory, FilenameUtils.getName(modUrl.getPath()))), StandardCharsets.UTF_8);
            System.out.println("Downloading to " + pathToDownload);

            modDownloader.downloadFileAsync(modUrl).thenAcceptAsync(response -> {
                try (FileOutputStream fos = new FileOutputStream(pathToDownload)) {
                    fos.write(Objects.requireNonNull(Objects.requireNonNull(response.body()).bytes()));
                    fos.flush();
                    fos.close();
                    completableFuture.complete(pathToDownload);
                    updateGoal.newFile = new File(pathToDownload);
                    updateGoal.postProcess();
                } catch (IOException e) {
                    completableFuture.completeExceptionally(e);
                }
                Objects.requireNonNull(response).close();
            }).exceptionally(ex -> {
                completableFuture.completeExceptionally(ex);
                return null;});
        } catch (MalformedURLException e) {
            completableFuture.completeExceptionally(e);
        }
        return completableFuture;
    }

    public CompletableFuture<Response> downloadMod(ListModData modData) throws MalformedURLException {
        return modDownloader.downloadFileAsync(new URL(modData.getFile_url()));
    }
}
