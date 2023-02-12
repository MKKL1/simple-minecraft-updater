package com.mkkl.mcupdater;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        MainOkno mainOkno = new MainOkno();
        mainOkno.initalizeComponents();
//        client.dispatcher().executorService().shutdown();
    }
}