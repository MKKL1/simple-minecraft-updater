package com.mkkl.mcupdater;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//TODO save mods.json in desired place
public class Main {
    public static void main(String[] args) throws IOException {
        MainOkno mainOkno = new MainOkno();
        mainOkno.initalizeComponents();
//        client.dispatcher().executorService().shutdown();
    }
}