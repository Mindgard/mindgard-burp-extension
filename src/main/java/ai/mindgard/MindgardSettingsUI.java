package ai.mindgard;

import ai.mindgard.sandbox.Dataset;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;


public class MindgardSettingsUI extends JPanel {
    private final JFileChooser customDatasetField = new JFileChooser();
    private File customDataset;
    private MindgardSettings settings;
    Map<Component, Object> originalValues = new HashMap<>();
    List<JLabel> changedLabels = new ArrayList<>();

    public MindgardSettingsUI() {
        
        super(new SpringLayout());

        this.settings = getSettings();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTabbedPane tabs = new JTabbedPane();
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel testConfigPanel = new JPanel(new GridBagLayout());
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints inputgbc = new GridBagConstraints();
        GridBagConstraints logingbc = new GridBagConstraints();
        inputgbc.gridx = 0;
        logingbc.gridx = 0;

        inputgbc.gridy = 0;
        logingbc.gridy = 0;

        inputgbc.anchor = GridBagConstraints.WEST;
        logingbc.anchor = GridBagConstraints.WEST;

        inputgbc.insets = new Insets(0, 0, 10, 5);
        logingbc.insets = new Insets(0, 0, 10, 5);


        JLabel selectorLabel = new JLabel("Selector:");
        testConfigPanel.add(selectorLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.fill = GridBagConstraints.HORIZONTAL;
        inputgbc.weightx = 1.0;
        JTextField selectorField = new JTextField(settings.selector(), 20);
        testConfigPanel.add(selectorField, inputgbc);
        setupUIChangeTracking(selectorField, selectorLabel);

        inputgbc.gridx = 0;
        inputgbc.gridy = 1;
        inputgbc.weightx = 0;
        JLabel projectIDLabel = new JLabel("Project ID:");
        testConfigPanel.add(projectIDLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.weightx = 1.0;
        JTextField projectIDField = new JTextField(settings.projectID(), 20);
        testConfigPanel.add(projectIDField, inputgbc);
        setupUIChangeTracking(projectIDField, projectIDLabel);

        inputgbc.gridx = 0;
        inputgbc.gridy = 2;
        inputgbc.weightx = 0;
        JLabel datasetLabel = new JLabel("Domain:");
        testConfigPanel.add(datasetLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.gridy = 2;
        inputgbc.weightx = 1.0;
        JComboBox<Dataset> datasetField = new JComboBox<>(Dataset.values());
        datasetField.setSelectedIndex(Dataset.indexOfName(settings.dataset()));
        testConfigPanel.add(datasetField, inputgbc);
        setupUIChangeTracking(datasetField, datasetLabel);


        inputgbc.gridx = 0;
        inputgbc.gridy = 3;
        inputgbc.weightx = 0;
        JLabel systemPromptLabel = new JLabel("System Prompt:");
        testConfigPanel.add(systemPromptLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.gridy = 3;
        inputgbc.weightx = 1.0;
        JTextArea systemPromptField = new JTextArea(settings.systemPrompt(), 5, 20);
        testConfigPanel.add(systemPromptField, inputgbc);
        setupUIChangeTracking(systemPromptField, systemPromptLabel);


        inputgbc.gridx = 0;
        inputgbc.gridy = 4;
        inputgbc.weightx = 0;
        JLabel customDatasetLabel = new JLabel("Custom Dataset:");
        testConfigPanel.add(customDatasetLabel, inputgbc);
        inputgbc.gridx = 1;
        inputgbc.gridy = 4;
        inputgbc.weightx = 1.0;
        var customDatasetPathLabel = new JLabel("No file selected");
        Optional.ofNullable(customDataset).map(Object::toString).ifPresent(customDatasetPathLabel::setText);

        testConfigPanel.add(customDatasetPathLabel, inputgbc);
        customDatasetPathLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.GRAY),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        inputgbc.gridx = 1;
        inputgbc.gridy = 5;
        inputgbc.weightx = 1.0;
        var datasetButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        var chooseButton = new JButton("Select File…");

        chooseButton.addActionListener(actionEvent -> {
            Optional.ofNullable(customDataset).ifPresent(customDatasetField::setSelectedFile);

            int result = customDatasetField.showOpenDialog(MindgardSettingsUI.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = customDatasetField.getSelectedFile();
                customDatasetPathLabel.setText(selectedFile.getAbsolutePath());
                customDataset = selectedFile;
            }
        });
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0,5,0,0);

        datasetButtons.add(chooseButton, constraints);

        inputgbc.gridx = 1;
        inputgbc.gridy = 5;
        inputgbc.weightx = 1.0;

        var clearButton = new JButton("Remove Dataset");

        clearButton.addActionListener(actionEvent -> {
            customDatasetPathLabel.setText("No file selected");
            customDataset = null;
            customDatasetField.setSelectedFile(null);
        });

        datasetButtons.add(clearButton, constraints);
        testConfigPanel.add(datasetButtons,inputgbc);
        setupUIChangeTracking(customDatasetPathLabel, customDatasetLabel);

        inputgbc.gridx = 0;
        inputgbc.gridy = 6;
        inputgbc.weightx = 0;
        JLabel excludeAttacksLabel = new JLabel("Exclude attack(s):");
        testConfigPanel.add(excludeAttacksLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.gridy = 6;
        inputgbc.weightx = 1.0;
        JTextField excludeAttacksField = new JTextField(settings.exclude(), 20);
        excludeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        testConfigPanel.add(excludeAttacksField, inputgbc);
        setupUIChangeTracking(excludeAttacksField, excludeAttacksLabel);

        inputgbc.gridx = 0;
        inputgbc.gridy = 7;
        inputgbc.weightx = 0;
        JLabel includeAttacksLabel = new JLabel("Include attack(s):");
        testConfigPanel.add(includeAttacksLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.gridy = 7;
        inputgbc.weightx = 1.0;
        JTextField includeAttacksField = new JTextField(settings.include(), 20);
        includeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        testConfigPanel.add(includeAttacksField, inputgbc);
        setupUIChangeTracking(includeAttacksField, includeAttacksLabel);

        inputgbc.gridx = 0;
        inputgbc.gridy = 8;
        inputgbc.weightx = 0;
        JLabel promptRepeatsLabel = new JLabel("Prompt Repeats:");
        testConfigPanel.add(promptRepeatsLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.gridy = 8;
        inputgbc.weightx = 1.0;
        JFormattedTextField promptRepeatsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        promptRepeatsField.setValue(settings.promptRepeats());
        promptRepeatsField.setToolTipText("e.g. 3");
        testConfigPanel.add(promptRepeatsField, inputgbc);
        setupUIChangeTracking(promptRepeatsField, promptRepeatsLabel);

        inputgbc.gridx = 0;
        inputgbc.gridy = 9;
        inputgbc.weightx = 0;
        JLabel parallelismLabel = new JLabel("Parallelism:");
        testConfigPanel.add(parallelismLabel, inputgbc);

        inputgbc.gridx = 1;
        inputgbc.gridy = 9;
        inputgbc.weightx = 1.0;
        JFormattedTextField parallelismField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        parallelismField.setValue(settings.parallelism());
        parallelismField.setToolTipText("e.g. 1");
        testConfigPanel.add(parallelismField, inputgbc);
        setupUIChangeTracking(parallelismField, parallelismLabel);

        inputgbc.gridx = 0;
        inputgbc.gridy = 10;
        inputgbc.gridwidth = 2;
        inputgbc.weightx = 0;
        JLabel parallelismLabelDescription = new JLabel("(Parallelism controls the maximum number of attacks we will launch against your model at once. If your model is stateful or otherwise would be confused by overlapping attacks, leave this at the default of 1.)");
        parallelismLabelDescription.setForeground(Color.decode("#CC5500"));
        testConfigPanel.add(parallelismLabelDescription, inputgbc);

        logingbc.gridx = 0;
        logingbc.fill = GridBagConstraints.HORIZONTAL;
        logingbc.weightx = 0;
        JLabel urlFieldLabel = new JLabel("Mindgard URL:");
        loginPanel.add(urlFieldLabel, logingbc);

        logingbc.gridx = 1;
        logingbc.gridy = 0;
        logingbc.weightx = 1.0;
        JTextField urlField = new JTextField(settings.url(), 20);
        includeAttacksField.setToolTipText("e.g. https://<YOUR DOMAIN>.mindgard.ai");
        loginPanel.add(urlField, logingbc);
        setupUIChangeTracking(urlField, urlFieldLabel);

        logingbc.gridx = 0;
        logingbc.gridy = 1;
        logingbc.weightx = 0;
        JLabel audienceFieldLabel = new JLabel("Audience:");
        loginPanel.add(audienceFieldLabel, logingbc);

        logingbc.gridx = 1;
        logingbc.gridy = 1;
        logingbc.weightx = 1.0;
        JTextField audienceField = new JTextField(settings.audience(), 20);
        includeAttacksField.setToolTipText("e.g. https://<YOUR AUDIENCE>.com");
        loginPanel.add(audienceField, logingbc);
        setupUIChangeTracking(audienceField, audienceFieldLabel);

        logingbc.gridx = 0;
        logingbc.gridy = 2;
        logingbc.weightx = 0;
        JLabel clientIDFieldLabel = new JLabel("Client ID:");
        loginPanel.add(clientIDFieldLabel, logingbc);

        logingbc.gridx = 1;
        logingbc.gridy = 2;
        logingbc.weightx = 1.0;
        JTextField clientIDField = new JTextField(settings.clientID(), 20);
        includeAttacksField.setToolTipText("Mindgard Client ID");
        loginPanel.add(clientIDField, logingbc);
        setupUIChangeTracking(clientIDField, clientIDFieldLabel);


        inputgbc.gridx = 0;
        inputgbc.gridy = 11;
        inputgbc.weightx = 1.0;
        JPanel testConfigButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((actionEvent) -> {
            saveUIContentsToSettings(
                selectorField.getText(),
                projectIDField.getText(),
                ((Dataset)datasetField.getSelectedItem()).getDatasetName(), 
                systemPromptField.getText(),
                Optional.ofNullable(customDataset).map(File::getAbsolutePath).orElse(null),
                excludeAttacksField.getText(),
                includeAttacksField.getText(),
                Integer.parseInt(promptRepeatsField.getText()),
                Integer.parseInt(parallelismField.getText()),
                urlField.getText(),
                audienceField.getText(),
                clientIDField.getText()
            );
        });
        logingbc.gridx = 0;
        logingbc.gridy = 3;
        logingbc.weightx = 0;
        JPanel loginButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener((actionEvent) -> {
            var auth = new MindgardAuthentication(settings);
            var deviceCode = auth.get_device_code();
            try {
                Desktop desktop = Desktop.getDesktop();
                String url = deviceCode.verification_uri_complete();
                desktop.browse(new java.net.URI(url));
                JOptionPane.showMessageDialog(this, "Confirm that you see " + deviceCode.user_code());
                auth.validate_login(deviceCode);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to open browser: " + e.getMessage(),
                        "Error",
                        JOptionPane.INFORMATION_MESSAGE
                        );
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Logged in successfully to " + settings.url()
            );
        });
        loginButtonPanel.add(loginButton);
        testConfigButtonPanel.add(saveButton);

        loginPanel.add(loginButtonPanel, logingbc);
        testConfigPanel.add(testConfigButtonPanel, inputgbc);

        tabs.addTab("Login", loginPanel);
        tabs.addTab("Test Configuration", testConfigPanel);
        mainPanel.add(tabs, BorderLayout.NORTH);
        
        add(mainPanel, BorderLayout.NORTH);
        
        setPreferredSize(new Dimension(300, 120));
    }

    /**
     * Wraps and saves the contents of the UI components.
     * This method saves the current contents of each UI component in the extension to the mindgard settings file.
     * @param selector contents of the selector field
     * @param projectID contents of the projectID field
     * @param dataset chosen dataset domain
     * @param systemPrompt contents of the system prompt field
     * @param customDatasetPath the path to the custom dataset on disk
     * @param exclude the attacks to exclude from the test
     * @param incude the attacks to include in the test
     * @param promptRepeats the number of times prompts are repeated in each attack
     * @param parallelism the maximum amount of concurrent requests on the target 
     */
    private void saveUIContentsToSettings(
        String selector,
        String projectID,
        String dataset,
        String systemPrompt,
        String customDatasetPath,
        String exclude,
        String incude,
        Integer promptRepeats,
        Integer parallelism,
        String urlString,
        String audienceString,
        String clientIDString
     ) {
        MindgardSettings newSettings = new MindgardSettings(
            urlString,
            audienceString,
            clientIDString,
            selector,
            projectID,
            dataset, 
            systemPrompt,
            customDatasetPath,
            exclude,
            incude,
            promptRepeats,
            parallelism
        );
        setSettings(newSettings);
        getSettings();
        

        if (!settings.save(Constants.SETTINGS_FILE_NAME)) {
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Mindgard settings updated successfully!" + "\n" +
                        "Tests run using this configuration can be found at " + urlString + "/results?project_id=" + projectID,
                "Mindgard Extension",
                JOptionPane.INFORMATION_MESSAGE
        );

        changedLabels.forEach(label -> {
            label.setForeground(Color.BLACK);
        });
    }

    /**
     * Sets up change tracking for a given UI field and its associated label.
     * 
     * This method attaches listeners to the provided field (which can be a {@link JTextField},
     * {@link JComboBox}, {@link JTextArea}, or {@link JLabel}) to monitor for changes in its value.
     * When a change is detected compared to the original value, the label's foreground color is updated
     * to indicate that the field has been modified. If the field is reverted to its original value,
     * the label color is reset.
     * 
     *
     * @param field the UI component to monitor for changes (must be a {@link JTextField}, {@link JComboBox},
     *              {@link JTextArea}, or {@link JLabel})
     * @param label the label associated with the field, whose color will be updated to reflect changes
     */
    private void setupUIChangeTracking(JComponent field, JLabel label) {
            if (field instanceof JTextField textField) {
                originalValues.put(textField, textField.getText());
                textField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) { checkChanged(); }
                    public void removeUpdate(DocumentEvent e) { checkChanged(); }
                    public void changedUpdate(DocumentEvent e) { checkChanged(); }
                    private void checkChanged() {
                        String original = (String) originalValues.get(textField);
                        if (!textField.getText().equals(original)) {
                            label.setForeground(Color.decode("#CC5500"));
                            changedLabels.add(label);
                        } else {
                            label.setForeground(Color.BLACK);
                        }
                    }
                });
            } else if (field instanceof JComboBox comboBox) {
                originalValues.put(comboBox, comboBox.getSelectedItem());
                comboBox.addActionListener(e -> {
                    Object original = originalValues.get(comboBox);
                    if (!Objects.equals(comboBox.getSelectedItem(), original)) {
                        label.setForeground(Color.decode("#CC5500"));
                        changedLabels.add(label);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                });
            } else if (field instanceof JTextArea textArea) {
                originalValues.put(textArea, textArea.getText());
                textArea.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) { checkChanged(); }
                    public void removeUpdate(DocumentEvent e) { checkChanged(); }
                    public void changedUpdate(DocumentEvent e) { checkChanged(); }
                    private void checkChanged() {
                        String original = (String) originalValues.get(textArea);
                        if (!textArea.getText().equals(original)) {
                            label.setForeground(Color.decode("#CC5500"));
                            changedLabels.add(label);
                        } else {
                            label.setForeground(Color.BLACK);
                        }
                    }
                });
            } else if (field instanceof JLabel labelField) {
                originalValues.put(labelField, labelField.getText());
                labelField.addPropertyChangeListener("text", e -> {
                    String original = (String) originalValues.get(labelField);
                    if (!labelField.getText().equals(original)) {
                        label.setForeground(Color.decode("#CC5500"));
                        changedLabels.add(label);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                });
            }
        }

    /**
     * Saves new settings contents to the settings file.
     * @param newSettings the new settings
    */
    public static void setSettings(MindgardSettings newSettings) {newSettings.save(Constants.SETTINGS_FILE_NAME);}
   
   /**
    * Gets the settings from disk
    * @return the populated MindgardSettings record
    */
    public static MindgardSettings getSettings() {return MindgardSettings.loadOrCreate(Constants.SETTINGS_FILE_NAME);}

}
