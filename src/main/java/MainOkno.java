import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class MainOkno {
    private JTextField modsjsonField;
    public JPanel mainPanel;
    private JComboBox<FileLocationType> comboBox1;
    private JButton button1;
    private JTextField modDirField;
    private JButton stworzPaczkeButton;
    private JTextField instancesDirField;
    private JTextField instanceNameField;

    ObjectMapper mapper = new ObjectMapper();
    OkHttpClient client = new OkHttpClient();
    ModList modList;
    String modDir;

    public MainOkno() {
        button1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(modList==null) getModList();
                modDir = modDirField.getText();
                try {
                    ModUpdater modUpdater = new ModUpdater(client, modDir);
                    modUpdater.verifyModList(modList);
                    modUpdater.updateMods(modList);


                    try (FileWriter fileWriter = new FileWriter("mods.json")) {
                        mapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, modList);
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    client.dispatcher().executorService().shutdown();
                }
            }
        });

        stworzPaczkeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(modList==null) getModList();
                CrystalLauIntegration crystalLauIntegration = new CrystalLauIntegration();
                crystalLauIntegration.setInstancesDir(instancesDirField.getText());
                try {
                    Path modPath = crystalLauIntegration.createEmptyInstance(instanceNameField.getText(), modList.getFbcmlversion());
                    modDirField.setText(modPath.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void createUIComponents() {
        comboBox1 = new JComboBox<FileLocationType>(FileLocationType.values());
    }

    private boolean getModList() {
        if (modsjsonField.getText().isBlank()) {
            modsjsonField.setBorder(new LineBorder(Color.red, 2));
            return false;
        }
        modsjsonField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        switch ((FileLocationType) Objects.requireNonNull(comboBox1.getSelectedItem())) {
            case NET -> {
                Request request = new Request.Builder()
                        .url(modsjsonField.getText())
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    modList = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), ModList.class);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            case LOCAL -> {
                try(FileReader fileReader = new FileReader(modsjsonField.getText())) {
                    modList = mapper.readValue(fileReader, ModList.class);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }
}
