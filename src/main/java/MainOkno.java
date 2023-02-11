import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.prefs.Preferences;

public class MainOkno {
    private JTextField modsjsonField;
    public JPanel mainPanel;
    private JComboBox<FileLocationType> comboBox1;
    private JButton button1;
    private JTextField modDirField;
    private JButton stworzPaczkeButton;
    private JTextField instancesDirField;
    private JTextField instanceNameField;
    private JRadioButton localSaveRadioButton;

    private JList<ListModData> modListGui;
    private JButton installButton;
    private JButton resetButton;
    private DefaultListModel<ListModData> defModListModel;
    java.util.List<ListModData> modsToUpdate;

    private final Preferences prefs = Preferences.userRoot().node(this.getClass().getName());;

    ObjectMapper mapper = new ObjectMapper();
    OkHttpClient client = new OkHttpClient();
    ModList modList;
    String modDir;

    public void savePreferences() {
        prefs.put("modDir", modDirField.getText());
        prefs.put("instancesDir", instancesDirField.getText());
        prefs.put("modsjson", modsjsonField.getText());
        prefs.put("locType", ((FileLocationType) Objects.requireNonNull(comboBox1.getSelectedItem())).name());
    }

    public void getPreferences() {
        modDirField.setText(prefs.get("modDir", "mods"));
        instancesDirField.setText(prefs.get("instancesDir", ""));
        modsjsonField.setText(prefs.get("modsjson", GlobalConfig.pathOrUrlToModList));
        comboBox1.setSelectedItem(FileLocationType.valueOf(prefs.get("locType", GlobalConfig.fileLocationType.name())));
    }

    public MainOkno() {
        getPreferences();

        button1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(modList==null && !getModList()) return;
                if(modsToUpdate != null && !modsToUpdate.isEmpty()) return;
                modDir = modDirField.getText();
                try {
                    ModUpdater modUpdater = new ModUpdater(client, modDir);
                    modUpdater.verifyModList(modList);
                    modsToUpdate = modUpdater.getModsToUpdate(modList);
                    if(modsToUpdate != null) defModListModel.addAll(modsToUpdate);

                    if(localSaveRadioButton.isSelected()) {
                        try (FileWriter fileWriter = new FileWriter("mods.json")) {
                            mapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, modList);
                        }
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        stworzPaczkeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(modList==null && !getModList()) return;
                CrystalLauIntegration crystalLauIntegration = new CrystalLauIntegration();
                String instanceName = instanceNameField.getText().strip().replaceAll(" ", "_");
                instanceNameField.setText(instanceName);
                String instancesDir = instancesDirField.getText().strip();
                if (!Path.of(instancesDir).endsWith("instances")) {
                    instancesDirField.setBorder(new LineBorder(Color.red, 2));
                    JOptionPane.showMessageDialog(mainPanel, "Zła ścieżka do instacji");
                    return;
                }
                instancesDirField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
                crystalLauIntegration.setInstancesDir(instancesDir);
                try {
                    Path modPath = crystalLauIntegration.createEmptyInstance(instanceName, modList.getFbcmlversion());
                    modDirField.setText(modPath.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        installButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(modsToUpdate == null) return;
                ModInstaller modInstaller = new ModInstaller(client, modDir);
                ListIterator<ListModData> iter = modsToUpdate.listIterator();
                while(iter.hasNext()) {
                    ListModData modData = iter.next();
                    modInstaller.installMod(modData).thenAccept(x -> defModListModel.removeElement(modData)).exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
                }

            }
        });
        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                modsjsonField.setText(GlobalConfig.pathOrUrlToModList);
                comboBox1.setSelectedItem(GlobalConfig.fileLocationType);
            }
        });
    }

    private void createUIComponents() {
        comboBox1 = new JComboBox<FileLocationType>(FileLocationType.values());
        defModListModel = new DefaultListModel<>();
        modListGui = new JList<>(defModListModel);
    }

    private boolean getModList() {
        if (modsjsonField.getText().isBlank()) {
            modsjsonField.setBorder(new LineBorder(Color.red, 2));
            JOptionPane.showMessageDialog(mainPanel, "Podaj sciezke lub url do listy modów");
            return false;
        }
        switch ((FileLocationType) Objects.requireNonNull(comboBox1.getSelectedItem())) {
            case NET -> {
                Request request = new Request.Builder()
                        .url(modsjsonField.getText())
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        modsjsonField.setBorder(new LineBorder(Color.red, 2));
                        JOptionPane.showMessageDialog(mainPanel, "Złe url do list modów");
                        return false;
                    }
                    modList = mapper.readValue(Objects.requireNonNull(response.body()).byteStream(), ModList.class);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            case LOCAL -> {
                try(FileReader fileReader = new FileReader(modsjsonField.getText())) {
                    modList = mapper.readValue(fileReader, ModList.class);
                } catch (FileNotFoundException ex) {
                    modsjsonField.setBorder(new LineBorder(Color.red, 2));
                    JOptionPane.showMessageDialog(mainPanel, "Taki plik z listą modów nie istnieje");
                    return false;
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        modsjsonField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        return true;
    }
}
