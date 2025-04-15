package ai.mindgard;

import ai.mindgard.sandbox.Dataset;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class MindgardSettingsUI extends JPanel implements MindgardSettings {
    private static final int DEFAULT_PROMPT_REPEATS = 5;
    private final JFileChooser customDatasetField = new JFileChooser();
    private File customDataset;
    private String selector = "$";
    private String testName = "burp-suite-test";
    private String dataset = null;
    private String systemPrompt = "Please answer the question: ";
    private String exclude;
    private String include;
    private Integer promptRepeats;

    public MindgardSettingsUI() {
        super(new SpringLayout());

        try (Stream<String> lines = Files.lines(MindgardSettings.file("burp.json").toPath())) {
            Settings settings = JSON.fromJson(lines.collect(joining()), Settings.class);
            selector = Optional.ofNullable(settings.selector()).orElse(selector);
            testName = Optional.ofNullable(settings.testName()).orElse(testName);
            dataset = Optional.ofNullable(settings.dataset()).orElse(dataset);
            systemPrompt = Optional.ofNullable(settings.systemPrompt()).orElse(systemPrompt);
            customDataset = Optional.ofNullable(settings.customDatasetFilename()).map(File::new).orElse(customDataset);
            exclude = Optional.ofNullable(settings.exclude()).orElse(exclude);
            include = Optional.ofNullable(settings.include()).orElse(include);
            promptRepeats = Optional.ofNullable(settings.promptRepeats()).orElse(DEFAULT_PROMPT_REPEATS);
        } catch (IOException e) {

        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 5);

        JLabel selectorLabel = new JLabel("Selector:");
        inputPanel.add(selectorLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField selectorField = new JTextField(selector, 20);
        inputPanel.add(selectorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel testNameLabel = new JLabel("Test Name:");
        inputPanel.add(testNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField testNameField = new JTextField(testName, 20);
        inputPanel.add(testNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel datasetLabel = new JLabel("Dataset:");
        inputPanel.add(datasetLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        JComboBox<Dataset> datasetField = new JComboBox<>(Dataset.values());
        datasetField.setSelectedIndex(Dataset.indexOfName(dataset));
        inputPanel.add(datasetField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        JLabel systemPromptLabel = new JLabel("System Prompt:");
        inputPanel.add(systemPromptLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        JTextArea systemPromptField = new JTextArea(systemPrompt, 5, 20);
        inputPanel.add(systemPromptField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        JLabel customDatasetLabel = new JLabel("Custom Dataset:");
        inputPanel.add(customDatasetLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        var customDatasetPathLabel = new JLabel("No file selected");
        Optional.ofNullable(customDataset).map(Object::toString).ifPresent(customDatasetPathLabel::setText);

        inputPanel.add(customDatasetPathLabel, gbc);
        customDatasetPathLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.GRAY),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
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

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;

        var clearButton = new JButton("Remove Dataset");

        clearButton.addActionListener(actionEvent -> {
            customDatasetPathLabel.setText("No file selected");
            customDataset = null;
            customDatasetField.setSelectedFile(null);
        });

        datasetButtons.add(clearButton, constraints);
        inputPanel.add(datasetButtons,gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0;
        JLabel excludeAttacksLabel = new JLabel("Exclude attack(s):");
        inputPanel.add(excludeAttacksLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        JTextField excludeAttacksField = new JTextField(exclude, 20);
        excludeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        inputPanel.add(excludeAttacksField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0;
        JLabel includeAttacksLabel = new JLabel("Include attack(s):");
        inputPanel.add(includeAttacksLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        JTextField includeAttacksField = new JTextField(include, 20);
        includeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        inputPanel.add(includeAttacksField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0;
        JLabel promptRepeatsLabel = new JLabel("Prompt Repeats:");
        inputPanel.add(promptRepeatsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.weightx = 1.0;
        JFormattedTextField promptRepeatsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        promptRepeatsField.setValue(promptRepeats);
        includeAttacksField.setToolTipText("e.g. 3");
        inputPanel.add(promptRepeatsField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((actionEvent) -> {
            selector = selectorField.getText();
            testName = testNameField.getText();
            systemPrompt = systemPromptField.getText();
            dataset = ((Dataset)datasetField.getSelectedItem()).getDatasetName();
            customDataset = customDatasetField.getSelectedFile();
            exclude = excludeAttacksField.getText();
            include =includeAttacksField.getText();
            promptRepeats =(Integer)promptRepeatsField.getValue();

            save();
        });
        buttonPanel.add(saveButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(300, 120));
    }

    public String selector() {
        return selector;
    }
    public String testName() {
        return testName;
    }

    @Override
    public String dataset() {
        return dataset;
    }

    @Override
    public String systemPrompt() {
        return systemPrompt;
    }

    @Override
    public String customDatasetFilename() {
        return Optional.ofNullable(customDataset).map(File::getAbsolutePath).orElse(null);
    }

    @Override
    public String exclude() {
        return exclude;
    }

    @Override
    public String include() {
        return include;
    }

    @Override
    public Integer promptRepeats() { return promptRepeats; }
}
