package com.mkkl.mcupdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class MainOkno extends JFrame {
    private JTextField modsjsonField;
    public JPanel mainPanel;
    private JComboBox<FileLocationType> comboBox1;
    private JButton button1;
    private JTextField modDirField;
    private JButton stworzPaczkeButton;
    private JTextField instancesDirField;
    private JTextField instanceNameField;
    private JRadioButton localSaveRadioButton;

    private JList<AddUpdateGoal> modListGui;
    private JButton installButton;
    private JButton resetButton;
    private JButton configCopyButton;
    private JComboBox<String> instancesList;
    DefaultComboBoxModel<String> instancesListModel = new DefaultComboBoxModel<String>();
    private DefaultListModel<AddUpdateGoal> defModListModel;
    List<AddUpdateGoal> modsToUpdate;

    CrystalLauIntegration crystalLauIntegration = new CrystalLauIntegration();

    private final Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

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
        instancesDirField.setText(prefs.get("instancesDir", CrystalLauIntegration.findInstancesDir()));
        modsjsonField.setText(prefs.get("modsjson", GlobalConfig.pathOrUrlToModList));
        comboBox1.setSelectedItem(FileLocationType.valueOf(prefs.get("locType", GlobalConfig.fileLocationType.name())));
    }

    public void initalizeComponents() {
        getPreferences();

        button1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (modList == null && !getModList()) return;
                if (modsToUpdate != null && !modsToUpdate.isEmpty()) return;
                modDir = modDirField.getText();
                if (!new File(modDir).exists()) {
                    modDirField.setBorder(new LineBorder(Color.red, 2));
                    JOptionPane.showMessageDialog(mainPanel, "Taki folder nie istnieje");
                    return;
                } else modDirField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
                try {
                    ModUpdater modUpdater = new ModUpdater(client, modDir);
                    modUpdater.verifyModList(modList);
                    modsToUpdate = modUpdater.getModsToUpdate(modList);
                    if (modsToUpdate != null) defModListModel.addAll(modsToUpdate);
                    else {
                        JOptionPane.showMessageDialog(mainPanel, "Brak modów do instalacji");
                    }

                    if (localSaveRadioButton.isSelected()) {
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
                if (modList == null && !getModList()) return;
                String instanceName = instanceNameField.getText().strip().replaceAll(" ", "_");
                instanceNameField.setText(instanceName);
                File instancedirFile = setInstancesDir();
                if (instancedirFile == null) return;
                try {
                    Path modPath = crystalLauIntegration.createEmptyInstance(instanceName, modList.getFbcmlversion());
                    modDirField.setText(modPath.toString());
                    Desktop.getDesktop().open(instancedirFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        installButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (modsToUpdate == null) return;
                ModInstaller modInstaller = new ModInstaller(client, modDir);
                ListIterator<AddUpdateGoal> iter = modsToUpdate.listIterator();
                while (iter.hasNext()) {
                    AddUpdateGoal updateGoal = iter.next();
                    modInstaller.installMod(updateGoal).thenAccept(x -> {
                        defModListModel.removeElement(updateGoal);
                        if (defModListModel.isEmpty()) {
                            //Finished downloading all the mods
                            JOptionPane.showMessageDialog(mainPanel, "Wszystkie mody zostały probrane, możesz wyłączyć program");
                        }
                    }).exceptionally(ex -> {
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

        configCopyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (instancesListModel.getSize() == 0) {
                    JOptionPane.showMessageDialog(mainPanel, "Nie wybrano paczki");
                    return;
                }
                String instancesDir = crystalLauIntegration.getInstancesDir();
                String nameToCopy = (String) instancesList.getSelectedItem();
                String copyFrom = Path.of(instancesDir, nameToCopy, ".minecraft").toString();

                String instanceName = instanceNameField.getText().strip().replaceAll(" ", "_");
                instanceNameField.setText(instanceName);

                String copyTo = Path.of(instancesDir, "u." + instanceName, ".minecraft").toString();
                System.out.println("Kopiowanie ustawień z '" + copyFrom + "' do '" + copyTo + "'");

                int choosenOption = JOptionPane.showConfirmDialog(
                        mainPanel,
                        "Kopiowanie ustawień z:" + System.lineSeparator() + "'" + copyFrom + "' do" + System.lineSeparator() + "'" + copyTo + "'",
                        "Czy napewno jesteś pewny że jesteś pewny?",
                        JOptionPane.YES_NO_OPTION);
                if (choosenOption != 0) return;
                String[] dirsToCopy = {"saves", "schematics", "resourcepacks", "Distant_Horizons_server_data", "config"};
                String[] filesToCopy = {"options.txt", "servers.dat",};
                List<String> failedDirs = new ArrayList<>();
                List<String> successfulDirs = new ArrayList<>();
                for (String dirToCopy : dirsToCopy) {
                    try {
                        FileUtils.copyDirectory(Path.of(copyFrom, dirToCopy).toFile(), Path.of(copyTo, dirToCopy).toFile());
                        successfulDirs.add(dirToCopy);
                    } catch (IOException ex) {
                        failedDirs.add(dirToCopy);
                        ex.printStackTrace();
                    }
                }

                for (String fileToCopy : filesToCopy) {
                    try {
                        FileUtils.copyFile(Path.of(copyFrom, fileToCopy).toFile(), Path.of(copyTo, fileToCopy).toFile());
                        successfulDirs.add(fileToCopy);
                    } catch (IOException ex) {
                        failedDirs.add(fileToCopy);
                        ex.printStackTrace();
                    }
                }

                JOptionPane.showMessageDialog(mainPanel, "Udało się kopiowanie:" + System.lineSeparator() +
                        successfulDirs.stream().map(s -> " - " + s).collect(Collectors.joining(System.lineSeparator())) +
                        System.lineSeparator() + "Nie udało się:" + System.lineSeparator() +
                        failedDirs.stream().map(s -> " - " + s).collect(Collectors.joining(System.lineSeparator())));
            }
        });
    }

    public MainOkno() {
        super("Updater");
        $$$setupUI$$$();
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                savePreferences();
                client.dispatcher().executorService().shutdown();
            }
        });
        instancesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (crystalLauIntegration.getInstancesDir() == null) {
                    setInstancesDir();
                }
                instancesListModel.removeAllElements();
                instancesListModel.addAll(crystalLauIntegration.getListOfInstaces());
                super.mouseClicked(e);
            }
        });
    }

    private File setInstancesDir() {
        String instancesDir = instancesDirField.getText().strip();
        if (instancesDir.isEmpty()) {
            instancesDir = CrystalLauIntegration.findInstancesDir();
            instancesDirField.setText(instancesDir);
        }
        File instancedirFile = new File(instancesDir);
        if (!Path.of(instancesDir).endsWith("instances") || !instancedirFile.exists() || !instancedirFile.isDirectory()) {
            instancesDirField.setBorder(new LineBorder(Color.red, 2));
            JOptionPane.showMessageDialog(mainPanel, "Zła ścieżka do instacji");
            return null;
        }
        instancesDirField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        crystalLauIntegration.setInstancesDir(instancesDir);
        return instancedirFile;
    }

    private void createUIComponents() {
        comboBox1 = new JComboBox<FileLocationType>(FileLocationType.values());
        defModListModel = new DefaultListModel<>();
        modListGui = new JList<>(defModListModel);
        instancesList = new JComboBox<>(instancesListModel);
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
                try (FileReader fileReader = new FileReader(modsjsonField.getText())) {
                    modList = mapper.readValue(fileReader, ModList.class);
                } catch (FileNotFoundException ex) {
                    modsjsonField.setBorder(new LineBorder(Color.red, 2));
                    JOptionPane.showMessageDialog(mainPanel, "Taki plik z listą modów nie istnieje");
                    return false;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        modsjsonField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        return true;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(9, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(486, 327), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Sciezka do instacji");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        instancesDirField = new JTextField();
        instancesDirField.setText(".../instances");
        panel2.add(instancesDirField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Nazwa paczki");
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        instanceNameField = new JTextField();
        instanceNameField.setText("super_paczka");
        panel2.add(instanceNameField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        stworzPaczkeButton = new JButton();
        stworzPaczkeButton.setText("Stworz paczke");
        panel1.add(stworzPaczkeButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("url or path");
        panel3.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("mods dir");
        panel3.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modsjsonField = new JTextField();
        modsjsonField.setText("mods.json");
        panel3.add(modsjsonField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        modDirField = new JTextField();
        modDirField.setText("mods");
        panel3.add(modDirField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panel3.add(comboBox1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Mod list source");
        panel3.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        localSaveRadioButton = new JRadioButton();
        localSaveRadioButton.setText("Zapisz mods.json lokalnie");
        panel3.add(localSaveRadioButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resetButton = new JButton();
        resetButton.setText("Reset");
        panel3.add(resetButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button1 = new JButton();
        button1.setText("Update mods");
        panel1.add(button1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Crystal Launcher tworzenie paczki");
        panel1.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Instaler modów");
        panel1.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Kopiowanie konfiguracji");
        panel1.add(label8, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        configCopyButton = new JButton();
        configCopyButton.setText("Kopiuj");
        panel1.add(configCopyButton, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Kopiuj z");
        panel4.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel4.add(instancesList, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Mody do pobrania");
        panel5.add(label10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        installButton = new JButton();
        installButton.setText("Pobierz");
        panel5.add(installButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        panel5.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(modListGui);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
