import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("FrameDemo");
        MainOkno mainOkno = new MainOkno();
        frame.setContentPane(mainOkno.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                mainOkno.savePreferences();
                mainOkno.client.dispatcher().executorService().shutdown();
            }
        });
//        client.dispatcher().executorService().shutdown();
    }
}